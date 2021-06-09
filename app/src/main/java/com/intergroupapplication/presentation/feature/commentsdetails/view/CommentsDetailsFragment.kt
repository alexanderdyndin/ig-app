package com.intergroupapplication.presentation.feature.commentsdetails.view

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.core.view.postDelayed
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.danikula.videocache.HttpProxyCacheServer
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.material.appbar.AppBarLayout
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.intergroupapplication.R
import com.intergroupapplication.databinding.FragmentCommentsDetailsBinding
import com.intergroupapplication.domain.entity.*
import com.intergroupapplication.domain.exception.FieldException
import com.intergroupapplication.domain.exception.NotFoundException
import com.intergroupapplication.domain.exception.TEXT
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.base.adapter.PagingLoadingAdapter
import com.intergroupapplication.presentation.customview.AudioGalleryView
import com.intergroupapplication.presentation.customview.ImageGalleryView
import com.intergroupapplication.presentation.customview.VideoGalleryView
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.*
import com.intergroupapplication.presentation.feature.commentsdetails.adapter.CommentDividerItemDecorator
import com.intergroupapplication.presentation.feature.commentsdetails.adapter.CommentsAdapter
import com.intergroupapplication.presentation.feature.commentsdetails.presenter.CommentsDetailsPresenter
import com.intergroupapplication.presentation.feature.commentsdetails.viewmodel.CommentsViewModel
import com.intergroupapplication.presentation.feature.group.adapter.GroupPostsAdapter
import com.intergroupapplication.presentation.feature.group.di.GroupViewModule.Companion.COMMENT_POST_ENTITY
import com.intergroupapplication.presentation.feature.mainActivity.view.MainActivity
import com.intergroupapplication.presentation.feature.mediaPlayer.AudioPlayerView
import com.intergroupapplication.presentation.feature.mediaPlayer.IGMediaService
import com.intergroupapplication.presentation.feature.mediaPlayer.VideoPlayerView
import com.intergroupapplication.presentation.feature.news.adapter.NewsAdapter
import com.intergroupapplication.presentation.listeners.RightDrawableListener
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mobsandgeeks.saripaar.ValidationError
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.NotEmpty
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.exceptions.CompositeException
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import org.w3c.dom.Text

import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext


class CommentsDetailsFragment() : BaseFragment(), CommentsDetailsView, Validator.ValidationListener,
        AppBarLayout.OnOffsetChangedListener, CoroutineScope{

    companion object {
        const val COMMENTS_DETAILS_REQUEST = 0
        const val COMMENTS_COUNT_VALUE = "COMMENTS_COUNT"
        const val GROUP_ID_VALUE = "GROUP_ID"

        private const val GROUP_ID = "group_id"
        private const val COMMENT_ID = "comment_id"
        const val POST_ID = "post_id"
        const val COMMENT_PAGE = "page"
    }

    private val viewBinding by viewBinding(FragmentCommentsDetailsBinding::bind)

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private var job : Job = Job()

    @Inject
    @InjectPresenter
    lateinit var presenter: CommentsDetailsPresenter

    @ProvidePresenter
    fun providePresenter(): CommentsDetailsPresenter = presenter


    @Inject
    lateinit var imageLoadingDelegate: ImageLoadingDelegate

    @Inject
    lateinit var validator: Validator

    @Inject
    lateinit var rightDrawableListener: RightDrawableListener


    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var proxyCacheServer: HttpProxyCacheServer

    @Inject
    lateinit var adapter: CommentsAdapter

    @Inject
    lateinit var adapterAd: ConcatAdapter

    @Inject
    @Named("footer")
    lateinit var adapterFooter: PagingLoadingAdapter

    lateinit var viewModel: CommentsViewModel

    var infoForCommentEntity: InfoForCommentEntity? = null

    var postId: String? = null

    var page: String = "1"

    var commentCreated = false

    @NotEmpty(messageResId = R.string.comment_should_contain_text)
    lateinit var commentEditText: AppCompatEditText

    private var groupPostEntity: GroupPostEntity.PostEntity? = null
    private var lastRepliedComment: CommentEntity.Comment? = null

    @LayoutRes
    override fun layoutRes() = R.layout.fragment_comments_details

    override fun getSnackBarCoordinator() = viewBinding.coordinator

    private lateinit var commentsList: RecyclerView
    private lateinit var settingsPost: ImageView
    private lateinit var swipeLayout: SwipyRefreshLayout
    private lateinit var imageBody: ImageGalleryView
    private lateinit var audioBody: AudioGalleryView
    private lateinit var videoBody: VideoGalleryView
    private lateinit var loading_layout: FrameLayout
    private lateinit var emptyText: TextView
    private lateinit var nestedScrollComments: NestedScrollView
    private lateinit var appbar: AppBarLayout
    private lateinit var postPrescription: TextView
    private lateinit var postText: TextView
    private lateinit var idpGroupPost: TextView
    private lateinit var commentBtn: TextView
    private lateinit var groupName: TextView
    private lateinit var postLike: TextView
    private lateinit var postDislike: TextView
    private lateinit var postLikesClickArea: FrameLayout
    private lateinit var postDislikesClickArea: FrameLayout
    private lateinit var subCommentBtn: TextView
    private lateinit var postAvatarHolder: SimpleDraweeView
    private lateinit var headerPostFromGroup: ConstraintLayout
    private lateinit var commentHolder: LinearLayout
    private lateinit var commentLoader: ProgressBar

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, modelFactory)[CommentsViewModel::class.java]
        lifecycleScope.newCoroutineContext(this.coroutineContext)
        infoForCommentEntity = arguments?.getParcelable(COMMENT_POST_ENTITY)
        postId = arguments?.getString(POST_ID)
        page = arguments?.getString(COMMENT_PAGE)!!
        if (infoForCommentEntity != null) {
            compositeDisposable.add(
                    viewModel.fetchComments(infoForCommentEntity!!.groupPostEntity.id, page).subscribe {
                        adapter.submitData(lifecycle, it)
                    }
            )
        } else if (postId != null) {
            compositeDisposable.add(
                    viewModel.fetchComments(postId!!, page).subscribe {
                        adapter.submitData(lifecycle, it)
                    }
            )
        }

    }

    override fun viewCreated() {
        commentsList = viewBinding.commentsList
        settingsPost = viewBinding.post.settingsPost
        swipeLayout = viewBinding.swipeLayout
        imageBody = viewBinding.post.imageBody
        audioBody = viewBinding.post.audioBody
        videoBody = viewBinding.post.videoBody
        loading_layout = viewBinding.loadingLayout
        emptyText = viewBinding.emptyText
        nestedScrollComments = viewBinding.nestedScrollComments
        appbar = viewBinding.appbar
        postPrescription = viewBinding.post.postPrescription
        postText = viewBinding.post.postText
        idpGroupPost = viewBinding.post.idpGroupPost
        commentBtn = viewBinding.post.commentBtn
        groupName = viewBinding.post.groupName
        postLike = viewBinding.post.postLike
        postDislike = viewBinding.post.postDislike
        postLikesClickArea = viewBinding.post.postLikesClickArea
        postDislikesClickArea = viewBinding.post.postDislikesClickArea
        subCommentBtn = viewBinding.post.subCommentBtn
        postAvatarHolder = viewBinding.post.postAvatarHolder
        headerPostFromGroup = viewBinding.post.headerPostFromGroup
        commentHolder = viewBinding.commentHolder
        commentLoader = viewBinding.commentLoader
        commentEditText = viewBinding.commentEditText
        val decorator = CommentDividerItemDecorator(requireContext())
        prepareEditText()
        //crashing app when provide it by dagger
        //commentsList.layoutManager = layoutManager
        commentsList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        //commentsList.setHasFixedSize(true)
        commentsList.itemAnimator = null
        commentsList.addItemDecoration(decorator)
        prepareAdapter()
        manageDataFlow(infoForCommentEntity)
        controlCommentEditTextChanges()
        //setSupportActionBar(toolbar)
        //supportActionBar?.setDisplayShowTitleEnabled(false)
        viewBinding.toolbarAction.setOnClickListener { findNavController().popBackStack() }
        ViewCompat.setNestedScrollingEnabled(commentsList, false)
        settingsPost.clicks()
                .subscribe { showPopupMenu(settingsPost) }
                .also { compositeDisposable.add(it) }

        newpaging()

        swipeLayout.setOnRefreshListener {
            groupPostEntity?.let { presenter.getPostDetailsInfo(it.id) }
            adapter.refresh()
        }
        audioBody.proxy = proxyCacheServer
        videoBody.proxy = proxyCacheServer

        imageBody.imageClick = { list, index ->
            val data = bundleOf("images" to list.toTypedArray(), "selectedId" to index)
            findNavController().navigate(R.id.action_commentsDetailsActivity_to_imageFragment, data)
        }
    }

    fun newpaging() {
        commentsList.adapter = adapterAd
        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
                if (job.isCancelled) return@collectLatest
                when (loadStates.refresh) {
                    is LoadState.Loading -> {
                        if (adapter.itemCount == 0) {
                            loading_layout.show()
                        }
                        emptyText.hide()
                        commentEditText.isEnabled = false
                    }
                    is LoadState.Error -> {
                        swipeLayout.isRefreshing = false
                        emptyText.hide()
                        loading_layout.gone()
                        if (adapter.itemCount == 0) {
                            adapterFooter.loadState = LoadState.Error((loadStates.refresh as LoadState.Error).error)
                        }
                        errorHandler.handle((loadStates.refresh as LoadState.Error).error)
                        commentEditText.isEnabled = true
                    }
                    is LoadState.NotLoading -> {
                        if (adapter.itemCount == 0) {
                            emptyText.show()
                        } else {
                            emptyText.hide()
                        }
                        loading_layout.gone()
                        swipeLayout.isRefreshing = false
                        if (commentCreated) {
                            nestedScrollComments.postDelayed(250) {
                                nestedScrollComments.smoothScrollTo(0, commentsList.bottom)
                            }
                            commentCreated = false
                        }
                        commentEditText.isEnabled = true
                        swipeLayout.isRefreshing = false
                    }
                    else -> {
                        swipeLayout.isRefreshing = false
                    }
                }
            }
        }
    }

    override fun hideSwipeLayout() {
        swipeLayout.isRefreshing = false
    }

    override fun onResume() {
        super.onResume()
        job = Job()
        appbar.addOnOffsetChangedListener(this)
    }

    override fun onPause() {
        super.onPause()
        job.cancel()
    }

    override fun onStop() {
        appbar.removeOnOffsetChangedListener(this)
        super.onStop()
    }


    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        swipeLayout.isEnabled = (verticalOffset == 0)
    }

    override fun showPostDetailInfo(groupPostEntity: GroupPostEntity.PostEntity) {
        setErrorHandler()
        this.groupPostEntity = groupPostEntity

        compositeDisposable.add(getDateDescribeByString(groupPostEntity.date)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ postPrescription.text = it }, { Timber.e(it) }))

        groupPostEntity.postText.let {
            if (it.isNotEmpty()) {
                postText.text = it
                postText.show()
            } else {
                postText.gone()
            }
        }

        idpGroupPost.text = requireContext().getString(R.string.idp, groupPostEntity.idp.toString())
        commentBtn.text = groupPostEntity.commentsCount
        groupName.text = groupPostEntity.groupInPost.name
        postDislike.text = groupPostEntity.reacts.dislikesCount.toString()
        postLike.text = groupPostEntity.reacts.likesCount.toString()
        subCommentBtn.text = groupPostEntity.bells.count.toString()

        if (groupPostEntity.reacts.isLike) {
            postLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_like_active, 0, 0, 0)
        } else {
            postLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_like, 0, 0, 0)
        }
        if (groupPostEntity.reacts.isDislike) {
            postDislike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_dislike_active, 0, 0, 0)
        } else {
            postDislike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_dislike, 0, 0, 0)
        }

        doOrIfNull(groupPostEntity.groupInPost.avatar, { imageLoadingDelegate.loadImageFromUrl(it, postAvatarHolder) },
                { imageLoadingDelegate.loadImageFromResources(R.drawable.variant_10, postAvatarHolder) })


        imageBody.setImages(groupPostEntity.images)
        videoBody.setVideos(groupPostEntity.videos)
        audioBody.setAudios(groupPostEntity.audios)

        subCommentBtn.setOnClickListener {
            if (!groupPostEntity.isLoading) {
                if (groupPostEntity.bells.isActive) {
                    compositeDisposable.add(viewModel.deleteBell(groupPostEntity.id)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnSubscribe { groupPostEntity.isLoading = true }
                            .doFinally {
                                groupPostEntity.isLoading = false
                                showPostDetailInfo(groupPostEntity)
                            }
                            .subscribe({
                                groupPostEntity.bells.isActive = false
                                groupPostEntity.bells.count--
                            }, { exception ->
                                if (exception is NotFoundException) {
                                    groupPostEntity.bells.isActive = false
                                    groupPostEntity.bells.count--
                                } else
                                    errorHandler.handle(exception)
                            }))
                } else {
                    compositeDisposable.add(viewModel.setBell(groupPostEntity.id)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnSubscribe { groupPostEntity.isLoading = true }
                            .doFinally {
                                groupPostEntity.isLoading = false
                                showPostDetailInfo(groupPostEntity)
                            }
                            .subscribe({
                                groupPostEntity.bells.isActive = true
                                groupPostEntity.bells.count++

                            }, { exception ->
                                if (exception is CompositeException) {
                                    exception.exceptions.forEach { ex ->
                                        (ex as? FieldException)?.let {
                                            if (it.field == "post") {
                                                groupPostEntity.bells.isActive = true
                                                groupPostEntity.bells.count++
                                            }
                                        }
                                    }
                                } else
                                    errorHandler.handle(exception)
                            }))
                }
            }
        }
        postLikesClickArea.setOnClickListener {
            if (!groupPostEntity.isLoading) {
                compositeDisposable.add(viewModel.setReact(isLike = !groupPostEntity.reacts.isLike,
                        isDislike = groupPostEntity.reacts.isDislike, postId = groupPostEntity.id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe {
                            groupPostEntity.isLoading = true
                        }
                        .doFinally {
                            groupPostEntity.isLoading = false
                            showPostDetailInfo(groupPostEntity)
                        }
                        .subscribe({
                            groupPostEntity.reacts = it
                        },
                                {
                                    errorHandler.handle(it)
                                }))
            }
        }
        postDislikesClickArea.setOnClickListener {
            if (!groupPostEntity.isLoading) {
                compositeDisposable.add(viewModel.setReact(isLike = groupPostEntity.reacts.isLike,
                        isDislike = !groupPostEntity.reacts.isDislike, postId = groupPostEntity.id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe {
                            groupPostEntity.isLoading = true
                        }
                        .doFinally {
                            groupPostEntity.isLoading = false
                            showPostDetailInfo(groupPostEntity)
                        }
                        .subscribe({
                            groupPostEntity.reacts = it
                        },
                                {
                                    errorHandler.handle(it)
                                }))
            }
        }
    }



    override fun commentCreated(commentEntity: CommentEntity) {
        adapter.refresh()
        commentCreated = true
        increaseCommentsCounter()
    }

    override fun answerToCommentCreated(commentEntity: CommentEntity) {
        adapter.refresh()
        commentCreated = true
        increaseCommentsCounter()
        if (commentHolder.childCount > 1) {
            commentHolder.removeViewAt(0)
        }
    }

//    override fun commentsLoaded(comments: PagedList<CommentEntity>) {
        //adapter.submitList(comments)
        //adapter.notifyDataSetChanged()
        //commentsList.smoothScrollToPosition(adapter.itemCount - 1)
//    }

    override fun onValidationFailed(errors: MutableList<ValidationError>) {
        for (error in errors) {
            val message = error.getCollatedErrorMessage(requireContext())
            dialogDelegate.showErrorSnackBar(message)
        }
    }

    override fun onValidationSucceeded() {
        if (commentHolder.childCount > 1) {
            lastRepliedComment?.let {
                presenter.createAnswerToComment(it.id, CreateCommentEntity(commentEditText.text.toString().trim()))
            }
        } else {
            groupPostEntity?.let { presenter.createComment(it.id, CreateCommentEntity(commentEditText.text.toString().trim())) }
        }
    }

//    override fun showLoading(show: Boolean) {
//        if (show) {
            //commentEditText.isEnabled = false
            //swipeLayout.isRefreshing = true
            //emptyText.hide()
            //adapter.removeError()
            //adapter.addLoading()
//        } else {
            //commentEditText.isEnabled = true
            //swipeLayout.isRefreshing = false
            //adapter.removeLoading()
//        }
//    }

    override fun showCommentUploading(show: Boolean) {
        if (show) {
            commentEditText.isEnabled = false
            commentLoader.show()
        } else {
            commentLoader.hide()
            commentEditText.isEnabled = true
        }
    }

    override fun showMessage(value: Int) {
        Toast.makeText(requireContext(), value, Toast.LENGTH_SHORT).show()
    }

    private fun setErrorHandler() {
        errorHandler.on(CompositeException::class.java) { throwable, _ ->
            run {
                (throwable as? CompositeException)?.exceptions?.forEach { ex ->
                    (ex as? FieldException)?.let {
                        if (it.field == TEXT) {
                            showErrorMessage(it.message.orEmpty())
                        }
                    }
                }
            }
        }
    }

    private fun increaseCommentsCounter() {
        var commentsCount = commentBtn.text.toString().toInt()
        commentsCount++
        commentBtn.text = commentsCount.toString()
        findNavController().previousBackStackEntry?.savedStateHandle?.set(COMMENTS_COUNT_VALUE, commentsCount.toString())
    }

    private fun controlCommentEditTextChanges() {
        RxTextView.afterTextChangeEvents(commentEditText)
                .subscribe {
                    val view = it.view()
                    if (view.text.trim().isEmpty()) {
                        view.setCompoundDrawablesWithIntrinsicBounds(null, null,
                                null, null)
                    } else {
                        view.setCompoundDrawablesWithIntrinsicBounds(null, null,
                                ContextCompat.getDrawable(requireContext(), R.drawable.ic_send), null)
                    }
                }
                .let(compositeDisposable::add)
        commentEditText.setOnTouchListener(rightDrawableListener)
        rightDrawableListener.clickListener = { validator.validate() }
    }

    private fun prepareAdapter() {
        with(CommentsAdapter) {
            replyListener = { comment ->
                if (!swipeLayout.isRefreshing) {
                    val view = layoutInflater.inflate(R.layout.reply_comment_layout, null)
                    if (commentHolder.childCount > 1) {
                        commentHolder.removeViewAt(0)
                    }
                    commentHolder.addView(view, 0)
                    view.findViewById<TextView>(R.id.responseToUser)
                            .apply {
                                setOnClickListener {
                                    commentHolder.removeView(view)
                                }
                                text = comment.commentOwner?.firstName
                                        ?: getString(R.string.unknown_user)
                                lastRepliedComment = comment.copy()
                            }
                    commentEditText.showKeyboard()
                }
            }
            complaintListener = { id -> presenter.complaintComment(id) }
            deleteClickListener = { id, pos ->
                compositeDisposable.add(viewModel.deleteComment(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ adapter.notifyItemRemoved(pos) },
                                { errorHandler.handle(it) })
                )
            }
            likeClickListener = { isLike, isDislike, comment, pos ->
                compositeDisposable.add(viewModel.setCommentReact(isLike, isDislike, comment.id.toInt())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            comment.reacts = it
                            adapter.notifyItemChanged(pos)
                        },
                                { errorHandler.handle(it) })
                )
            }
            USER_ID = userSession.user?.id?.toInt()
        }
    }

    private fun prepareEditText() {
        commentEditText.isVerticalScrollBarEnabled = true
        commentEditText.maxLines = 5
        commentEditText.setScroller(Scroller(requireContext()))
        commentEditText.movementMethod = ScrollingMovementMethod()
    }

    private fun manageDataFlow(infoForCommentEntity: InfoForCommentEntity?) {
        if (infoForCommentEntity != null) {
            groupPostEntity = infoForCommentEntity.groupPostEntity
            showPostDetailInfo(infoForCommentEntity.groupPostEntity)
            if (infoForCommentEntity.isFromNewsScreen) {
                headerPostFromGroup.setOnClickListener {
                    val data = bundleOf(GROUP_ID to infoForCommentEntity.groupPostEntity.groupInPost.id)
                    findNavController().navigate(R.id.action_commentsDetailsActivity_to_groupActivity, data)
                }
            }
        }
        else if (postId != null){
            presenter.getPostDetailsInfo(postId!!)
        }

    }


    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.inflate(R.menu.settings_menu)

        popupMenu.setOnMenuItemClickListener { menu ->
            when (menu.itemId) {
                R.id.complaint ->
                    groupPostEntity?.let { presenter.complaintPost(Integer.parseInt(it.id)) }

            }
            return@setOnMenuItemClickListener true
        }

        popupMenu.show()
    }
}
