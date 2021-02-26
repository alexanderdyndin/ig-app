package com.intergroupapplication.presentation.feature.commentsdetails.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.ViewGroup
import android.widget.Scroller
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.PagedList
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.CommentEntity
import com.intergroupapplication.domain.entity.CreateCommentEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.entity.InfoForCommentEntity
import com.intergroupapplication.domain.exception.FieldException
import com.intergroupapplication.domain.exception.TEXT
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.base.PagingView
import com.intergroupapplication.presentation.base.adapter.PagingLoadingAdapter
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.delegate.PagingDelegate
import com.intergroupapplication.presentation.exstension.*
import com.intergroupapplication.presentation.feature.commentsdetails.adapter.CommentDetailsAdapter
import com.intergroupapplication.presentation.feature.commentsdetails.adapter.CommentDividerItemDecorator
import com.intergroupapplication.presentation.feature.commentsdetails.adapter.CommentsAdapter
import com.intergroupapplication.presentation.feature.commentsdetails.presenter.CommentsDetailsPresenter
import com.intergroupapplication.presentation.feature.commentsdetails.viewmodel.CommentsViewModel
import com.intergroupapplication.presentation.feature.group.di.GroupViewModule.Companion.COMMENT_POST_ENTITY
import com.intergroupapplication.presentation.feature.group.view.GroupFragment.Companion.FRAGMENT_RESULT
import com.intergroupapplication.presentation.feature.news.viewmodel.NewsViewModel
import com.intergroupapplication.presentation.listeners.RightDrawableListener
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mobsandgeeks.saripaar.ValidationError
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.NotEmpty
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.exceptions.CompositeException
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_comments_details.*
import kotlinx.android.synthetic.main.fragment_comments_details.emptyText
import kotlinx.android.synthetic.main.item_group_post.*
import kotlinx.android.synthetic.main.reply_comment_layout.view.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named


class CommentsDetailsFragment(private val pagingDelegate: PagingDelegate) : BaseFragment(), CommentsDetailsView, Validator.ValidationListener,
        AppBarLayout.OnOffsetChangedListener, PagingView by pagingDelegate {

    constructor() : this(PagingDelegate())

    companion object {
        const val COMMENTS_DETAILS_REQUEST = 0
        const val COMMENTS_COUNT_VALUE = "COMMENTS_COUNT"
        const val GROUP_ID_VALUE = "GROUP_ID"

        private const val GROUP_ID = "group_id"
        private const val COMMENT_ID = "comment_id"
        private const val POST_ID = "post_id"
    }

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

//    @Inject
//    lateinit var adapter: CommentDetailsAdapter

    @Inject
    lateinit var layoutManager: RecyclerView.LayoutManager

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var adapter: CommentsAdapter

    @Inject
    lateinit var adapterAd: ConcatAdapter

    @Inject
    @Named("footer")
    lateinit var adapterFooter: PagingLoadingAdapter

    lateinit var viewModel: CommentsViewModel

    lateinit var infoForCommentEntity: InfoForCommentEntity

    var commentCreated = false

    @NotEmpty(messageResId = R.string.comment_should_contain_text)
    lateinit var commentEditText: AppCompatEditText

    private lateinit var groupPostEntity: GroupPostEntity
    private var lastRepliedComment: CommentEntity? = null

    @LayoutRes
    override fun layoutRes() = R.layout.fragment_comments_details

    override fun getSnackBarCoordinator(): ViewGroup? = coordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, modelFactory)[CommentsViewModel::class.java]
        infoForCommentEntity = requireArguments().getParcelable(COMMENT_POST_ENTITY)
        compositeDisposable.add(
                viewModel.fetchComments(infoForCommentEntity.groupPostEntity.id).subscribe {
                    adapter.submitData(lifecycle, it)
                }
        )
    }

    override fun viewCreated() {
        commentEditText = requireView().findViewById(R.id.commentEditText)
        val decorator = CommentDividerItemDecorator(requireContext())
        prepareEditText()
        //crashing app when provide it by dagger
        //commentsList.layoutManager = layoutManager
        commentsList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        commentsList.setHasFixedSize(true)
        commentsList.itemAnimator = null
        commentsList.addItemDecoration(decorator)
        prepareAdapter()
        manageDataFlow(infoForCommentEntity)
        controlCommentEditTextChanges()
        //setSupportActionBar(toolbar)
        //supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbarAction.setOnClickListener { findNavController().popBackStack() }
        //pagingDelegate.attachPagingView(adapter, swipeLayout, emptyText)
        ViewCompat.setNestedScrollingEnabled(commentsList, false)
        settingsPost.clicks()
                .subscribe { showPopupMenu(settingsPost) }
                .also { compositeDisposable.add(it) }
        CommentsAdapter.complaintListener = { id -> presenter.complaintComment(id) }
        newpaging()
    }

    fun newpaging() {
        swipeLayout.setOnRefreshListener {
            adapter.refresh()
        }
        commentsList.adapter = adapterAd
        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
                when(loadStates.refresh) {
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
                        if (adapter.itemCount == 0) {
                            emptyText.show()
                        } else {
                            emptyText.hide()
                        }
                        loading_layout.gone()
                        swipeLayout.isRefreshing = false
                        if (commentCreated) {
                            commentsList.scrollToPosition(adapter.itemCount)
                            commentCreated = false
                        }
                    }
                    else ->{ swipeLayout.isRefreshing = false }
                }
            }
        }
    }

    override fun hideSwipeLayout() {
        swipeLayout.isRefreshing = false
    }

    override fun onResume() {
        super.onResume()
        appbar.addOnOffsetChangedListener(this)
    }

    override fun onStop() {
        appbar.removeOnOffsetChangedListener(this)
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }


    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        swipeLayout.isEnabled = (verticalOffset == 0)
    }

    override fun showPostDetailInfo(groupPostEntity: GroupPostEntity) {
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
        postCommentsCount.text = groupPostEntity.commentsCount
        groupName.text = groupPostEntity.groupInPost.name
        groupPostEntity.photo?.let {
            postImage.show()
            imageLoadingDelegate.loadImageFromUrl(it, postImage)
        }
        //presenter.getPostComments(groupPostEntity.id)
        doOrIfNull(groupPostEntity.groupInPost.avatar, { imageLoadingDelegate.loadImageFromUrl(it, groupPostAvatar) },
                { imageLoadingDelegate.loadImageFromResources(R.drawable.application_logo, groupPostAvatar) })
        swipeLayout.setOnRefreshListener {
            presenter.getPostDetailsInfo(groupPostEntity.id)
        }
    }

    override fun commentCreated(commentEntity: CommentEntity) {
        //presenter.refresh(groupPostEntity.id)
        adapter.refresh()
        commentCreated = true
        increaseCommentsCounter()
        commentsList.smoothScrollToPosition(adapter.itemCount)
    }

    override fun answerToCommentCreated(commentEntity: CommentEntity) {
        //presenter.refresh(groupPostEntity.id)
        adapter.refresh()
        commentCreated = true
        increaseCommentsCounter()
        if (commentHolder.childCount > 1) {
            commentHolder.removeViewAt(0)
        }
        commentsList.smoothScrollToPosition(adapter.itemCount)
    }

    override fun commentsLoaded(comments: PagedList<CommentEntity>) {
        //adapter.submitList(comments)
        //adapter.notifyDataSetChanged()
        //commentsList.smoothScrollToPosition(adapter.itemCount - 1)
    }

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
            presenter.createComment(groupPostEntity.id, CreateCommentEntity(commentEditText.text.toString().trim()))
        }
    }

    override fun showLoading(show: Boolean) {
        if (show) {
            //commentEditText.isEnabled = false
            //swipeLayout.isRefreshing = true
            //emptyText.hide()
            //adapter.removeError()
            //adapter.addLoading()
        } else {
            //commentEditText.isEnabled = true
            //swipeLayout.isRefreshing = false
            //adapter.removeLoading()
        }
    }

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
        var commentsCount = postCommentsCount.text.toString().toInt()
        commentsCount++
        postCommentsCount.text = commentsCount.toString()
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
        CommentsAdapter.replyListener = { comment ->
            if (!swipeLayout.isRefreshing) {
                val view = layoutInflater.inflate(R.layout.reply_comment_layout, null)
                if (commentHolder.childCount > 1) {
                    commentHolder.removeViewAt(0)
                }
                commentHolder.addView(view, 0)
                view.responseToUser
                        .apply {
                            setOnClickListener {
                                commentHolder.removeView(view)
                            }
                            text = comment.commentOwner?.firstName
                                    ?: getString(R.string.unknown_user)
                            lastRepliedComment = CommentEntity(
                                    comment.id,
                                    comment.text,
                                    comment.date,
                                    comment.commentOwner,
                                    comment.answerTo
                            )
                        }
                commentEditText.showKeyboard()
            }
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
            showPostDetailInfo(groupPostEntity)
            if (infoForCommentEntity.isFromNewsScreen) {
                goToGroupClickArea.setOnClickListener {
                    val data = bundleOf(GROUP_ID to groupPostEntity.groupInPost.id)
                    findNavController().navigate(R.id.action_commentsDetailsActivity_to_groupActivity, data)
                    //presenter.goToGroupScreen(groupPostEntity.groupInPost.id)
                }
            }
        }
//        else {
//            presenter.getPostDetailsInfo(intent.getStringExtra(POST_ID)!!)
//        }

    }


    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.inflate(R.menu.settings_menu)

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.complaint ->
                    presenter.complaintPost(Integer.parseInt(groupPostEntity.id))
            }
            return@setOnMenuItemClickListener true
        }

        popupMenu.show()
    }
}
