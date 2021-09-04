package com.intergroupapplication.presentation.feature.group.view

import android.os.Bundle
import android.view.View
import android.view.ViewStub
import android.widget.*
import androidx.annotation.LayoutRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.appbar.AppBarLayout
import com.intergroupapplication.R
import com.intergroupapplication.databinding.FragmentGroupBinding
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.domain.entity.*
import com.intergroupapplication.domain.exception.FieldException
import com.intergroupapplication.domain.exception.NotFoundException
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.base.ImageUploader
import com.intergroupapplication.presentation.base.adapter.PagingLoadingAdapter
import com.intergroupapplication.presentation.customview.AvatarImageUploadingView
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.*
import com.intergroupapplication.presentation.feature.editpost.view.EditPostFragment
import com.intergroupapplication.presentation.feature.group.adapter.GroupPostsAdapter
import com.intergroupapplication.presentation.feature.group.presenter.GroupPresenter
import com.intergroupapplication.presentation.feature.group.viewmodel.GroupViewModel
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs


class GroupFragment : BaseFragment(), GroupView,
        AppBarLayout.OnOffsetChangedListener, CoroutineScope {

    companion object {
        private const val PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR = 0.9f
        private const val PERCENTAGE_TO_HIDE_TITLE_DETAILS = 0.3f
        private const val ALPHA_ANIMATIONS_DURATION = 200
        const val GROUP_ID = "group_id"
        const val GROUP = "group"
        const val IS_ADMIN = "is_admin"
        const val POST_ID = "post_id"
    }

    private val viewBinding by viewBinding(FragmentGroupBinding::bind)

    @Inject
    @InjectPresenter
    lateinit var presenter: GroupPresenter

    @ProvidePresenter
    fun providePresenter(): GroupPresenter = presenter

    private lateinit var groupId: String

    private var groupEntity: GroupEntity.Group? = null

    private var isAdmin = false

    @Inject
    lateinit var imageLoadingDelegate: ImageLoadingDelegate

    @Inject
    lateinit var imageUploadingDelegate: ImageUploader

    @Inject
    lateinit var adapter: GroupPostsAdapter

    @Inject
    lateinit var adapterAD: ConcatAdapter

    @Inject
    @Named("footer")
    lateinit var footerAdapter: PagingLoadingAdapter

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private var job : Job = Job()

    lateinit var viewModel: GroupViewModel

    private var mIsTheTitleVisible = false
    private var mIsTheTitleContainerVisible = true

    private var createdPostId: String? = null

    @LayoutRes
    override fun layoutRes() = R.layout.fragment_group

    override fun getSnackBarCoordinator(): CoordinatorLayout = viewBinding.adminGroupCoordinator

    private lateinit var groupPosts: RecyclerView
    private lateinit var groupAvatarHolder: AvatarImageUploadingView
    private lateinit var toolbarTittle: TextView
    private lateinit var appbar: AppBarLayout
    private lateinit var toolbarBackAction: ImageButton
    private lateinit var groupStrength: TextView
    private lateinit var swipeLayout: SwipeRefreshLayout
    private lateinit var emptyText: TextView
    private lateinit var loadingLayout: FrameLayout
    private lateinit var idGroup: TextView
    private lateinit var likesCount: TextView
    private lateinit var dislikesCount: TextView
    private lateinit var commentsCount: TextView
    private lateinit var postsCount: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var joinToGroup: Button
    private lateinit var goOutFromGroup: Button
    private lateinit var headGroupCreatePostViewStub: ViewStub
    private lateinit var createPost: Button
    private lateinit var headGroupJoinViewStub: ViewStub
    private lateinit var signingProgress: ProgressBar

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        groupId = arguments?.getString(GROUP_ID)!!
        groupEntity = arguments?.getParcelable(GROUP)
        viewModel = ViewModelProvider(this, modelFactory)[GroupViewModel::class.java]
        lifecycleScope.newCoroutineContext(this.coroutineContext)
        prepareAdapter()
        compositeDisposable.add(
                viewModel.fetchPosts(groupId)
                        .subscribe {
                            adapter.submitData(lifecycle, it)
                        }
        )
    }

    override fun viewCreated() {
        groupPosts = viewBinding.groupPosts
        groupAvatarHolder = viewBinding.group.groupAvatarHolder
        toolbarTittle = viewBinding.navigationToolbar.toolbarTittle
        appbar = viewBinding.appbar
        toolbarBackAction = viewBinding.navigationToolbar.toolbarBackAction
        groupStrength = viewBinding.group.groupStrength
        swipeLayout = viewBinding.swipeLayout
        emptyText = viewBinding.emptyText
        loadingLayout = viewBinding.loadingLayout
        idGroup = viewBinding.group.idGroup
        likesCount = viewBinding.group.likesCount
        dislikesCount = viewBinding.group.dislikesCount
        commentsCount = viewBinding.group.commentsCount
        postsCount = viewBinding.group.postsCount
        progressBar = viewBinding.progressBar
        headGroupCreatePostViewStub = viewBinding.group.headGroupCreatePostViewStub
        headGroupJoinViewStub = viewBinding.group.headGroupJoinViewStub

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>(POST_ID)?.observe(
                viewLifecycleOwner) { id ->
            if (createdPostId != id) {
                createdPostId = id
                adapter.refresh()
            }
        }
        groupPosts.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        groupPosts.itemAnimator = null
        groupAvatarHolder.imageLoaderDelegate = imageLoadingDelegate
        toolbarBackAction.setOnClickListener { findNavController().popBackStack() }
        appbar.addOnOffsetChangedListener(this)
        groupEntity?.let {
            showGroupInfo(it)
        }
        presenter.getGroupDetailInfo(groupId)
        groupStrength.setOnClickListener {
            val data = bundleOf(GROUP_ID to groupId, IS_ADMIN to isAdmin)
            findNavController().navigate(R.id.action_groupActivity_to_userListFragment, data)
        }
        newPaging()
    }

    private fun newPaging() {
        swipeLayout.setOnRefreshListener {
            adapter.refresh()
        }
        groupPosts.adapter = adapterAD
        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
                if (job.isCancelled) return@collectLatest
                when(loadStates.refresh) {
                    is LoadState.Loading -> {
                        if (adapter.itemCount == 0) {
                            loadingLayout.show()
                        }
                        emptyText.hide()
                    }
                    is LoadState.Error -> {
                        swipeLayout.isRefreshing = false
                        emptyText.hide()
                        loadingLayout.gone()
                        if (adapter.itemCount == 0) {
                            footerAdapter.loadState = LoadState.Error((loadStates.refresh as LoadState.Error).error)
                        }
                        errorHandler.handle((loadStates.refresh as LoadState.Error).error)
                    }
                    is LoadState.NotLoading -> {
                        if (adapter.itemCount == 0) {
                            emptyText.show()
                        } else {
                            emptyText.hide()
                        }
                        loadingLayout.gone()
                        swipeLayout.isRefreshing = false
                        if (createdPostId != null){
                            groupPosts.scrollToPosition(0)
                            createdPostId = null
                        }
                    }
                    else ->{ swipeLayout.isRefreshing = false }
                }
            }
        }
    }

    private fun prepareAdapter() {
        GroupPostsAdapter.apply {
            commentClickListener = { openCommentDetails(InfoForCommentEntity(it)) }
            complaintListener = { id -> presenter.complaintPost(id) }
            imageClickListener = { list: List<FileEntity>, i: Int ->
                val data = bundleOf("images" to list.toTypedArray(), "selectedId" to i)
                findNavController().navigate(R.id.action_groupActivity_to_imageFragment,data)
            }
            editPostClickListener = {
                val data = bundleOf(GROUP_ID to it.id, EditPostFragment.GROUP_POST_ENTITY_KEY to it)
                findNavController().navigate(R.id.action_groupActivity_to_editPostFragment,data)
            }
            likeClickListener = { like, dislike, item, position ->
                if (!item.isLoading) {
                    compositeDisposable.add(viewModel.setReact(isLike = like, isDislike = dislike, postId = item.id)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnSubscribe {
                                item.isLoading = true
                                adapter.notifyItemChanged(position)
                            }
                            .doFinally {
                                item.isLoading = false
                                adapter.notifyItemChanged(position)
                            }
                            .subscribe({
                                item.reacts = it
                            },
                                    {
                                        errorHandler.handle(it)
                                    }))
                }
            }
            deleteClickListener = { id: Int, pos: Int ->
                compositeDisposable.add(viewModel.deletePost(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            adapter.notifyItemRemoved(pos) //todo сделать человеческое удаление
                        }, { errorHandler.handle(it) })
                )
            }
            bellClickListener = { item: GroupPostEntity.PostEntity, pos: Int ->
                if (!item.isLoading) {
                    if (item.bells.isActive) {
                        compositeDisposable.add(viewModel.deleteBell(item.id)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnSubscribe { item.isLoading = true }
                                .doFinally {
                                    item.isLoading = false
                                    adapter.notifyItemChanged(pos)
                                }
                                .subscribe({
                                    item.bells.isActive = false
                                    item.bells.count--
                                }, { exception ->
                                    if (exception is NotFoundException) {
                                        item.bells.isActive = false
                                        item.bells.count--
                                    } else
                                        errorHandler.handle(exception)
                                }))
                    } else {
                        compositeDisposable.add(viewModel.setBell(item.id)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnSubscribe { item.isLoading = true }
                                .doFinally {
                                    item.isLoading = false
                                    adapter.notifyItemChanged(pos)
                                }
                                .subscribe({
                                    item.bells.isActive = true
                                    item.bells.count++
                                }, { exception ->
                                    if (exception is CompositeException) {
                                        exception.exceptions.forEach { ex ->
                                            (ex as? FieldException)?.let {
                                                if (it.field == "post") {
                                                    item.bells.isActive = true
                                                    item.bells.count++
                                                }
                                            }
                                        }
                                    } else
                                        errorHandler.handle(exception)
                                }))
                    }
                }
            }
            pinClickListener = { item: GroupPostEntity.PostEntity, pos: Int ->
                val post = item.copy(isPinned = !item.isPinned)
                compositeDisposable.add(viewModel.editPost(post)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe {  }
                        .doFinally { adapter.notifyItemChanged(pos) }
                        .subscribe({
                            item.isPinned = !item.isPinned
                            if (item.isPinned)
                                Toast.makeText(requireContext(), R.string.post_pinned, Toast.LENGTH_SHORT).show()
                            else
                                Toast.makeText(requireContext(), R.string.post_unpinned, Toast.LENGTH_SHORT).show()
                                   }, {
                            errorHandler.handle(it)
                        })
                )
            }
            progressBarVisibility = {visibility ->
                if (visibility){
                    viewBinding.progressDownload.show()
                }
                else{
                    viewBinding.progressDownload.gone()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appbar.addOnOffsetChangedListener(this)
        job = Job()
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
        val maxScroll = appBarLayout.totalScrollRange
        val percentage = abs(verticalOffset).toFloat() / maxScroll.toFloat()

        handleAlphaOnTitle(percentage)
        //handleToolbarTitleVisibility(percentage)
    }

    override fun changeCommentsCount(pair: Pair<String, String>) {
    }

    override fun renderViewByRole(userRole: UserRole) {
        when (userRole) {
            UserRole.ADMIN -> renderAdminPage()
            UserRole.USER_FOLLOWER -> renderUserPage(R.id.goOutFromGroup)
            UserRole.USER_NOT_FOLLOWER -> renderUserPage(R.id.joinToGroup)
        }
    }

    override fun showGroupInfo(groupEntity: GroupEntity.Group) {
        toolbarTittle.text = groupEntity.name
        //groupName.text = groupEntity.name
        idGroup.text = getString(R.string.idg, groupEntity.id)
        likesCount.text = groupEntity.postsLikes
        dislikesCount.text = groupEntity.postsDislikes
        commentsCount.text = groupEntity.CommentsCount
        postsCount.text = groupEntity.postsCount
        groupStrength.text = groupEntity.followersCount
        doOrIfNull(groupEntity.avatar, {
            groupAvatarHolder.showAvatar(it)
        }, { groupAvatarHolder.showAvatar(R.drawable.variant_10) })
        GroupPostsAdapter.isOwner = userSession.user?.id == groupEntity.owner
    }

    override fun showGroupInfoLoading(show: Boolean) {
        if (show) {
            progressBar.show()
        } else {
            progressBar.hide()
        }
    }

    override fun showImageUploadingStarted(chooseMedia: ChooseMedia) {
        groupAvatarHolder.showImageUploadingStartedWithoutFile()
    }

    override fun showImageUploaded(chooseMedia: ChooseMedia) {
        presenter.changeGroupAvatar(groupId)
    }

    override fun avatarChanged(url: String) {
        groupAvatarHolder.showAvatar(url)
        groupAvatarHolder.showImageUploaded()
        compositeDisposable.add(
            viewModel.fetchPosts(groupId)
                .subscribe {
                    adapter.submitData(lifecycle, it)
                })
    }

    override fun showImageUploadingProgress(progress: Float, chooseMedia: ChooseMedia) {
        groupAvatarHolder.showImageUploadingProgress(progress)
    }


    override fun showImageUploadingError(chooseMedia: ChooseMedia) {
        groupAvatarHolder.clearUploadingState()
    }

    override fun groupFollowed(followersCount: Int) {
        joinToGroup.hide()
        goOutFromGroup.show()
        groupStrength.text = followersCount.toString()
        listenButtonClicks()
        findNavController().previousBackStackEntry?.savedStateHandle?.set(GROUP_ID,
            GroupInfoEntity(groupId, groupStrength.text.toString(), true))
    }

    override fun groupUnfollowed(followersCount: Int) {
        goOutFromGroup.hide()
        joinToGroup.show()
        groupStrength.text = followersCount.toString()
        listenButtonClicks()
        findNavController().previousBackStackEntry?.savedStateHandle?.set(GROUP_ID,
            GroupInfoEntity(groupId, groupStrength.text.toString(), false))
    }

    override fun showMessage(res: Int) {
        Toast.makeText(requireContext(), res, Toast.LENGTH_SHORT).show()
    }

    override fun showMessage(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }


    private fun renderAdminPage() {
        isAdmin = true
        headGroupCreatePostViewStub.inflate()
        createPost = requireView().findViewById(R.id.createPost)
        createPost.setOnClickListener {
            openCreatePost(groupId)
        }
        groupAvatarHolder.setOnClickListener {
            if (groupAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.UPLOADED
                || groupAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.NONE) {
                dialogDelegate.showDialog(R.layout.dialog_camera_or_gallery,
                        mapOf(R.id.fromCamera to { presenter.attachFromCamera(groupId) },
                            R.id.fromGallery to { presenter.attachFromGallery(groupId) }))
            }
        }
        adapter.isAdmin = isAdmin
    }

    private fun renderUserPage(viewId: Int) {
        headGroupJoinViewStub.inflate()
        joinToGroup = requireView().findViewById(R.id.joinToGroup)
        goOutFromGroup = requireView().findViewById(R.id.goOutFromGroup)
        signingProgress = requireView().findViewById(R.id.signingProgress)
        listenButtonClicks()
        requireView().findViewById<Button>(viewId).show()
    }

    override fun groupFollowedError() {
        showSubscribeLoading(false)
        goOutFromGroup.hide()
        joinToGroup.show()
    }

    override fun groupUnfollowedError() {
        showSubscribeLoading(false)
        joinToGroup.hide()
        goOutFromGroup.show()
    }

    override fun showSubscribeLoading(show: Boolean) {
        if (show) {
            joinToGroup.hide()
            goOutFromGroup.hide()
            signingProgress.show()
        } else {
            signingProgress.hide()
        }
    }

    private fun openCommentDetails(entity: InfoForCommentEntity) {
        val data = bundleOf("comment_post" to entity)
        findNavController().navigate(R.id.action_groupActivity_to_commentsDetailsActivity, data)
    }

    private fun openCreatePost(post: String) {
        val data = bundleOf(GROUP_ID to post)
        findNavController().navigate(R.id.action_groupActivity_to_CreatePostFragment, data)
    }

    private fun handleToolbarTitleVisibility(percentage: Float) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {

            if (!mIsTheTitleVisible) {
                startAlphaAnimation(toolbarTittle, ALPHA_ANIMATIONS_DURATION.toLong(), View.VISIBLE)
                mIsTheTitleVisible = true
            }
        } else {
            if (mIsTheTitleVisible) {
                startAlphaAnimation(toolbarTittle, ALPHA_ANIMATIONS_DURATION.toLong(), View.INVISIBLE)
                mIsTheTitleVisible = false
            }
        }
    }

    private fun handleAlphaOnTitle(percentage: Float) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if (mIsTheTitleContainerVisible) {
                mIsTheTitleContainerVisible = false
            }
        } else {
            if (!mIsTheTitleContainerVisible) {
                mIsTheTitleContainerVisible = true
            }
        }
    }

    private fun listenButtonClicks() {
        RxView.clicks(joinToGroup)
                .take(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    presenter.followGroup(groupId, groupStrength.text.toString()
                        .split(" ")[0].toInt())
                }.let { { d: Disposable -> compositeDisposable.add(d) } }
        RxView.clicks(goOutFromGroup)
                .take(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    presenter.unfollowGroup(groupId, groupStrength.text.toString()
                        .split(" ")[0].toInt())
                }.let { { d: Disposable -> compositeDisposable.add(d) } }
    }

}