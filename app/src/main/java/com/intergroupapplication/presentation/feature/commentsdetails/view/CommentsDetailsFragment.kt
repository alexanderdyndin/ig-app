package com.intergroupapplication.presentation.feature.commentsdetails.view

import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.core.view.MotionEventCompat
import androidx.core.view.ViewCompat
import androidx.core.view.postDelayed
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.danikula.videocache.HttpProxyCacheServer
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.*
import com.intergroupapplication.domain.exception.FieldException
import com.intergroupapplication.domain.exception.NotFoundException
import com.intergroupapplication.domain.exception.TEXT
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.base.adapter.PagingLoadingAdapter
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.*
import com.intergroupapplication.presentation.feature.bottomsheet.presenter.BottomSheetPresenter
import com.intergroupapplication.presentation.feature.bottomsheet.view.AutoCloseBottomSheetBehavior
import com.intergroupapplication.presentation.feature.bottomsheet.view.BottomSheetFragment
import com.intergroupapplication.presentation.feature.commentsdetails.adapter.CommentDividerItemDecorator
import com.intergroupapplication.presentation.feature.commentsdetails.adapter.CommentsAdapter
import com.intergroupapplication.presentation.feature.commentsdetails.presenter.CommentsDetailsPresenter
import com.intergroupapplication.presentation.feature.commentsdetails.viewmodel.CommentsViewModel
import com.intergroupapplication.presentation.feature.group.di.GroupViewModule.Companion.COMMENT_POST_ENTITY
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.exceptions.CompositeException
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_comments_details.*
import kotlinx.android.synthetic.main.fragment_create_post.*
import kotlinx.android.synthetic.main.item_group_post.*
import kotlinx.android.synthetic.main.item_group_post.postText
import kotlinx.android.synthetic.main.item_group_post.view.*
import kotlinx.android.synthetic.main.item_input_comment.*
import kotlinx.android.synthetic.main.layout_attach_image.view.*
import kotlinx.android.synthetic.main.reply_comment_layout.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.roundToInt
import kotlin.coroutines.CoroutineContext


class CommentsDetailsFragment : BaseFragment(), CommentsDetailsView,
        AppBarLayout.OnOffsetChangedListener,BottomSheetFragment.Callback {

    companion object {
        const val COMMENTS_DETAILS_REQUEST = 0
        const val COMMENTS_COUNT_VALUE = "COMMENTS_COUNT"
        const val GROUP_ID_VALUE = "GROUP_ID"

        private const val GROUP_ID = "group_id"
        private const val COMMENT_ID = "comment_id"
        const val POST_ID = "post_id"
        const val COMMENT_PAGE = "page"
    }

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

    //@Inject
    //lateinit var validator: Validator

    //@Inject
    //lateinit var rightDrawableListener: RightDrawableListener


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

    private lateinit var bottomSheetBehaviour: AutoCloseBottomSheetBehavior<View>

    //private val loadingViews: MutableMap<String, View?> = mutableMapOf()

    //@NotEmpty(messageResId = R.string.comment_should_contain_text)
    //lateinit var commentEditText: AppCompatEditText

    private var groupPostEntity: GroupPostEntity.PostEntity? = null
    private var lastRepliedComment: CommentEntity.Comment? = null

    private val bottomFragment by lazy {BottomSheetFragment()}

    @LayoutRes
    override fun layoutRes() = R.layout.fragment_comments_details

    override fun getSnackBarCoordinator(): ViewGroup? = coordinator

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

    private fun convertDpToPixel(dp: Int): Int {
        val metrics: DisplayMetrics = Resources.getSystem().displayMetrics
        val px = dp * (metrics.densityDpi / 160f)
        return px.roundToInt()
    }

    override fun viewCreated() {
        try {
            childFragmentManager.beginTransaction().replace(R.id.containerBottomSheet, bottomFragment).commit()
            bottomFragment.callback = this
            bottomSheetBehaviour = BottomSheetBehavior.from(containerBottomSheet) as AutoCloseBottomSheetBehavior<View>
            bottomSheetBehaviour.run {
                peekHeight = convertDpToPixel(93)
                commentHolder.minimumHeight = peekHeight
                halfExpandedRatio = 0.6f
                isFitToContents = false
                addBottomSheetCallback(object : BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        bottomFragment.changeState(newState)
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {

                    }
                })
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        presenter.postId = postId
        val decorator = CommentDividerItemDecorator(requireContext())
        //prepareEditText()
        //crashing app when provide it by dagger
        //commentsList.layoutManager = layoutManager
        commentsList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        //commentsList.setHasFixedSize(true)
        commentsList.itemAnimator = null
        commentsList.addItemDecoration(decorator)
        prepareAdapter()
        manageDataFlow(infoForCommentEntity)
        //controlCommentEditTextChanges()
        //setSupportActionBar(toolbar)
        //supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbarAction.setOnClickListener { findNavController().popBackStack() }
        ViewCompat.setNestedScrollingEnabled(commentsList, false)
        settingsPost.clicks()
                .subscribe { showPopupMenu(settingsPost) }
                .also { compositeDisposable.add(it) }

        newPaging()

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

    fun newPaging() {
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
                        //bottomFragment.commentEditText.isEnabled = false
                    }
                    is LoadState.Error -> {
                        swipeLayout.isRefreshing = false
                        emptyText.hide()
                        loading_layout.gone()
                        if (adapter.itemCount == 0) {
                            adapterFooter.loadState = LoadState.Error((loadStates.refresh as LoadState.Error).error)
                        }
                        errorHandler.handle((loadStates.refresh as LoadState.Error).error)
                        // bottomFragment.commentEditText.isEnabled = true
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
                        //bottomFragment.commentEditText.isEnabled = true
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

        idpGroupPost.text = requireContext().getString(R.string.idp, groupPostEntity.id.toString())
        countComments.text = groupPostEntity.commentsCount
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


    override fun showCommentUploading(show: Boolean) {
        if (show) {
            //bottomFragment.commentEditText.isEnabled = false
            commentLoader.show()
        } else {
            commentLoader.hide()
            //bottomFragment.commentEditText.isEnabled = true
        }
    }

    override fun showMessage(value: Int) {
        Toast.makeText(requireContext(), value, Toast.LENGTH_SHORT).show()
    }

    override fun showImageUploadingStarted(path: String) {
        /*loadingViews[path] = layoutInflater.inflate(R.layout.layout_attach_image, postContainer1, false)
        loadingViews[path]?.let {
            it.imagePreview?.let { draweeView ->
                val type = MimeTypeMap.getFileExtensionFromUrl(path)
                val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(type) ?: ""
                if (mime in listOf("audio/mpeg", "audio/aac", "audio/wav")) {
                    imageLoadingDelegate.loadImageFromResources(R.drawable.variant_10, draweeView)
                    it.nameView?.text = path.substring(path.lastIndexOf("/") + 1)
                }
                else
                    imageLoadingDelegate.loadImageFromFile(path, draweeView)
            }
        }

        postContainer1.addView(loadingViews[path])
        prepareListeners(loadingViews[path], path)
        imageUploadingStarted(loadingViews[path])*/
    }

    override fun showImageUploaded(path: String) {
        /*loadingViews[path]?.apply {
            darkCard?.hide()
            stopUploading?.hide()
            imageUploadingProgressBar?.hide()
            detachImage?.show()
            commentEditText.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_send), null)
            setUpRightDrawableListener()
        }*/
    }

    override fun showImageUploadingProgress(progress: Float, path: String) {
        /*loadingViews[path]?.apply {
            imageUploadingProgressBar?.progress = progress
        }*/
    }

    override fun showImageUploadingError(path: String) {
        /*loadingViews[path]?.apply {
            darkCard?.show()
            detachImage?.show()
            refreshContainer?.show()
            imageUploadingProgressBar?.hide()
            stopUploading?.hide()
        }*/
    }

    override fun createComment(textComment: String, bottomPresenter: BottomSheetPresenter) {
        if (commentHolder.childCount > 1) {
            lastRepliedComment?.let {
                bottomPresenter.createAnswerToComment(it.id, textComment)
            }
        } else {
            bottomPresenter.createComment(groupPostEntity.id, textComment)
        }
    }


    override fun changeStateBottomSheet(newState: Int) {
        bottomSheetBehaviour.state = newState
    }

    override fun getState() = bottomSheetBehaviour.state

    override fun addHeightContainer(height: Int) {
        bottomSheetBehaviour.peekHeight = height
        commentHolder.minimumHeight = height
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
        var commentsCount = countComments.text.toString().toInt()
        commentsCount++
        commentBtn.text = commentsCount.toString()
        findNavController().previousBackStackEntry?.savedStateHandle?.set(COMMENTS_COUNT_VALUE, commentsCount.toString())
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
                    bottomFragment.answerComment(comment)
                    lastRepliedComment = comment.copy()
                   // bottomFragment.commentEditText.showKeyboard()
                }
            }
           imageClickListener = { list: List<FileEntity>, i: Int ->
                val data = bundleOf("images" to list.toTypedArray(), "selectedId" to i)
                findNavController().navigate(R.id.action_commentsDetailsActivity_to_imageFragment, data)
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
