package com.intergroupapplication.presentation.feature.commentsdetails.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.danikula.videocache.HttpProxyCacheServer
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.intergroupapplication.R
import com.intergroupapplication.data.network.PAGE_SIZE
import com.intergroupapplication.domain.entity.*
import com.intergroupapplication.domain.exception.FieldException
import com.intergroupapplication.domain.exception.TEXT
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.base.adapter.PagingLoadingAdapter
import com.intergroupapplication.presentation.customview.AutoCloseBottomSheetBehavior
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.*
import com.intergroupapplication.presentation.feature.commentsbottomsheet.presenter.BottomSheetPresenter
import com.intergroupapplication.presentation.feature.commentsbottomsheet.view.CommentBottomSheetFragment
import com.intergroupapplication.presentation.feature.commentsdetails.adapter.CommentDividerItemDecorator
import com.intergroupapplication.presentation.feature.commentsdetails.adapter.CommentsAdapter
import com.intergroupapplication.presentation.feature.commentsdetails.presenter.CommentsDetailsPresenter
import com.intergroupapplication.presentation.feature.commentsdetails.viewmodel.CommentsViewModel
import com.intergroupapplication.presentation.feature.group.di.GroupViewModule.Companion.COMMENT_POST_ENTITY
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_comment_bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_comments_details.*
import kotlinx.android.synthetic.main.fragment_create_post.*
import kotlinx.android.synthetic.main.item_group_post.*
import kotlinx.android.synthetic.main.item_group_post.view.*
import kotlinx.android.synthetic.main.item_input_comment.*
import kotlinx.android.synthetic.main.layout_attach_image.view.*
import kotlinx.android.synthetic.main.reply_comment_layout.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext


class CommentsDetailsFragment : BaseFragment(), CommentsDetailsView,CoroutineScope,
        AppBarLayout.OnOffsetChangedListener {

    companion object {
        const val COMMENTS_DETAILS_REQUEST = 0
        const val COMMENTS_COUNT_VALUE = "COMMENTS_COUNT"
        const val GROUP_ID_VALUE = "GROUP_ID"

        private const val GROUP_ID = "group_id"
        const val COMMENT_ID = "comment_id"
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

    private var infoForCommentEntity: InfoForCommentEntity? = null

    private var postId: String? = null

    private var page: String = "1"

    private var comment_id:String = "1"

    private var commentCreated = false

    private lateinit var bottomSheetBehaviour: AutoCloseBottomSheetBehavior<FrameLayout>

    private var groupPostEntity: CommentEntity.PostEntity? = null
    private var lastRepliedComment: CommentEntity.Comment? = null

    private val bottomFragment by lazy {CommentBottomSheetFragment()}

    private val scrollListener = object:RecyclerView.OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            val endPos = (commentsList.layoutManager as LinearLayoutManager)
                    .findLastCompletelyVisibleItemPosition()
            swipeLayout.isEnabled = endPos == adapter.itemCount-1
        }
    }

    @LayoutRes
    override fun layoutRes() = R.layout.fragment_comments_details

    override fun getSnackBarCoordinator(): ViewGroup? = coordinator

    private lateinit var disposable:Disposable

    @SuppressLint("CheckResult")
    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, modelFactory)[CommentsViewModel::class.java]
        lifecycleScope.newCoroutineContext(this.coroutineContext)
        infoForCommentEntity = arguments?.getParcelable(COMMENT_POST_ENTITY)
        postId = arguments?.getString(POST_ID)
        page = arguments?.getString(COMMENT_PAGE)?:"1"
        comment_id = arguments?.getString(COMMENT_ID)?:"1"
        disposable = CommentsViewModel.publishSubject.subscribe{
            when(it.first){
                CommentBottomSheetFragment.ADD_HEIGHT_CONTAINER->
                    addHeightContainer(it.second as Int)
                CommentBottomSheetFragment.ANSWER_COMMENT_CREATED_DATA->
                    answerToCommentCreated(it.second as CommentEntity)
                CommentBottomSheetFragment.COMMENT_CREATED_DATA->
                    commentCreated(it.second as CommentEntity)
                CommentBottomSheetFragment.CHANGE_STATE_BOTTOM_SHEET_DATA->
                    changeStateBottomSheet(it.second as Int)
                CommentBottomSheetFragment.CREATE_COMMENT_DATA-> {
                    val data = it.second as Pair<String,BottomSheetPresenter>
                    createComment(data.first,data.second)
                }
                CommentBottomSheetFragment.HIDE_SWIPE_DATA -> hideSwipeLayout()
                CommentBottomSheetFragment.SHOW_COMMENT_UPLOADING_DATA ->
                    showCommentUploading(it.second as Boolean)
            }
        }
    }

    override fun viewCreated() {
        try {
            childFragmentManager.beginTransaction().replace(R.id.containerCommentBottomSheet, bottomFragment).commit()
            bottomSheetBehaviour = BottomSheetBehavior.from(containerCommentBottomSheet) as AutoCloseBottomSheetBehavior<FrameLayout>
            bottomSheetBehaviour.run {
                peekHeight = requireContext().dpToPx(100)
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
        commentsList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        commentsList.itemAnimator = null
        commentsList.addItemDecoration(decorator)
        commentsList.addOnScrollListener(scrollListener)
        swipeLayout.isEnabled = false
        prepareAdapter()
        toolbarAction.setOnClickListener { findNavController().popBackStack() }

        newPaging()

        swipeLayout.setOnRefreshListener {
            groupPostEntity?.let { presenter.getPostDetailsInfo(it.id) }
            adapter.refresh()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
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
                    }
                    is LoadState.Error -> {
                        swipeLayout.isRefreshing = false
                        emptyText.hide()
                        loading_layout.gone()
                        if (adapter.itemCount == 0) {
                            adapterFooter.loadState = LoadState.Error((loadStates.refresh as LoadState.Error).error)
                        }
                        errorHandler.handle((loadStates.refresh as LoadState.Error).error)
                    }
                    is LoadState.NotLoading -> {
                        if (swipeLayout.isRefreshing){
                            commentsList.scrollToPosition(adapter.itemCount-1)
                            swipeLayout.isRefreshing = false
                        }
                        if (comment_id != "1" && adapter.itemCount != 0) {
                            val positionAnswerComment = adapter.positionAnswerComment(comment_id)
                            commentsList.scrollToPosition(positionAnswerComment)
                            comment_id = "1"
                        }
                        if (adapter.itemCount == 0) {
                            emptyText.show()
                        } else {
                            emptyText.hide()
                        }
                        loading_layout.gone()
                        if (commentCreated) {
                            commentsList.scrollToPosition(adapter.itemCount-1)
                            commentCreated = false
                        }
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
        manageDataFlow(infoForCommentEntity)
    }

    override fun onPause() {
        super.onPause()
        job.cancel()
    }


    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        swipeLayout.isEnabled = (verticalOffset == 0)
    }

    override fun showPostDetailInfo(groupPostEntity: CommentEntity.PostEntity) {
        setErrorHandler()
        this.groupPostEntity = groupPostEntity
        if (infoForCommentEntity != null) {
            compositeDisposable.add(
                    viewModel.fetchComments(groupPostEntity, page).subscribe {
                        page = (groupPostEntity.commentsCount.toInt()/20+1).toString()
                        adapter.submitData(lifecycle, it)
                    }
            )
        }
        else if (postId !=null){
            compositeDisposable.add(
                    viewModel.fetchComments(groupPostEntity, page).subscribe {
                        adapter.submitData(lifecycle, it)
                        page = (groupPostEntity.commentsCount.toInt()/20+1).toString()
                    }
            )
        }
    }



    private fun commentCreated(commentEntity: CommentEntity) {
        commentCreated = true
        increaseCommentsCounter()
        groupPostEntity?.let { presenter.getPostDetailsInfo(it.id) }
        adapter.refresh()
    }

    private fun answerToCommentCreated(commentEntity: CommentEntity) {
        commentCreated = true
        increaseCommentsCounter()
        if (commentHolder.childCount > 1) {
            commentHolder.removeViewAt(0)
        }
        groupPostEntity?.let { presenter.getPostDetailsInfo(it.id) }
        adapter.refresh()
    }


    private fun showCommentUploading(show: Boolean) {
        if (show) {
            commentLoader.show()
        } else {
            commentLoader.hide()
        }
    }

    override fun showMessage(value: Int) {
        Toast.makeText(requireContext(), value, Toast.LENGTH_SHORT).show()
    }

    private fun createComment(textComment: String, bottomPresenter: BottomSheetPresenter) {
        if (commentHolder.childCount > 1) {
            lastRepliedComment?.let {
                bottomPresenter.createAnswerToComment(it.id, textComment)
            }
        } else {
            bottomPresenter.createComment(groupPostEntity?.id.toString(), textComment)
        }
    }


    private fun changeStateBottomSheet(newState: Int) {
        if (newState == BottomSheetBehavior.STATE_HALF_EXPANDED){
            if (bottomSheetBehaviour.state == BottomSheetBehavior.STATE_COLLAPSED){
                bottomSheetBehaviour.state = newState
            }
        }
        else{
            bottomSheetBehaviour.state = newState
        }
    }

    private fun addHeightContainer(height: Int) {
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
        adapter.postInCommentHolder.increaseCommentsCounter()
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
            showPostDetailInfo = ::showPostDetailInfo
            clicksSettingPost = { settingsPost: ImageView ->
                settingsPost.clicks()
                        .subscribe { showPopupMenu(settingsPost) }
                        .also { compositeDisposable.add(it) }
            }
            changeCountComments = {count ->
                findNavController().previousBackStackEntry?.savedStateHandle
                        ?.set(COMMENTS_COUNT_VALUE, count.toString())
            }
            progressBarVisibility = {visibility ->
                if (visibility){
                    progressDownload.show()
                }
                else{
                    progressDownload.gone()
                }
            }
            USER_ID = userSession.user?.id?.toInt()
        }
    }

    private fun manageDataFlow(infoForCommentEntity: InfoForCommentEntity?) {
        if (infoForCommentEntity != null) {
            groupPostEntity = mapToCommentEntityPost(infoForCommentEntity.groupPostEntity)
            groupPostEntity?.let{showPostDetailInfo(it)}
            /*if (infoForCommentEntity.isFromNewsScreen) {
                headerPostFromGroup.setOnClickListener {
                    val data = bundleOf(GROUP_ID to infoForCommentEntity.groupPostEntity.groupInPost.id)
                    findNavController().navigate(R.id.action_commentsDetailsActivity_to_groupActivity, data)
                }
            }*/
        }
        else if (postId != null){
            presenter.getPostDetailsInfo(postId!!)
        }

    }

    private fun mapToCommentEntityPost(postEntity: GroupPostEntity.PostEntity) =
        CommentEntity.PostEntity(postEntity.id,postEntity.bells,
            postEntity.groupInPost,postEntity.postText,postEntity.date,postEntity.updated,postEntity.author,
            postEntity.pin,postEntity.photo,postEntity.commentsCount,postEntity.activeCommentsCount,
            postEntity.isActive,postEntity.isOffered,postEntity.isPinned,postEntity.reacts,
            postEntity.idp,postEntity.images,postEntity.audios,postEntity.videos,postEntity.isLoading,postEntity.unreadComments,
            postEntity.imagesExpanded,postEntity.audiosExpanded,postEntity.videosExpanded)

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
