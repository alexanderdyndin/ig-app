package com.intergroupapplication.presentation.feature.group.view

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.*
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.base.ImageUploader
import com.intergroupapplication.presentation.base.adapter.PagingLoadingAdapter
import com.intergroupapplication.presentation.customview.AvatarImageUploadingView
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.*
import com.intergroupapplication.presentation.feature.group.adapter.GroupPostsAdapter
import com.intergroupapplication.presentation.feature.group.presenter.GroupPresenter
import com.intergroupapplication.presentation.feature.group.viewmodel.GroupViewModel
import com.jakewharton.rxbinding2.view.RxView
import com.workable.errorhandler.Action
import com.workable.errorhandler.ErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.auth_loader.progressBar
import kotlinx.android.synthetic.main.creategroup_toolbar_layout.*
import kotlinx.android.synthetic.main.fragment_group.*
import kotlinx.android.synthetic.main.item_group_header_view.*
import kotlinx.android.synthetic.main.layout_admin_create_post_button.*
import kotlinx.android.synthetic.main.layout_user_join_button.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.abs


class GroupFragment() : BaseFragment(), GroupView,
        AppBarLayout.OnOffsetChangedListener {

    companion object {
        private const val PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR = 0.9f
        private const val PERCENTAGE_TO_HIDE_TITLE_DETAILS = 0.3f
        private const val ALPHA_ANIMATIONS_DURATION = 200
        const val GROUP_ID = "group_id"
        const val POST_ID = "post_id"
        const val FRAGMENT_RESULT = "fragmentResult"
        const val IS_GROUP_CREATED_NOW = "isGroupCreatedNow"
    }

//    private lateinit var mMediaBrowserCompat: MediaBrowserCompat                                                                                            // todo сделать более гибкое подключение коллбэков
//    private val connectionCallback: MediaBrowserCompat.ConnectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
//        override fun onConnected() {
//
//            // The browser connected to the session successfully, use the token to create the controller
//            super.onConnected()
//            mMediaBrowserCompat.sessionToken.also { token ->
//                val mediaController = MediaControllerCompat( this@GroupFragment.requireActivity(), token)
//                MediaControllerCompat.setMediaController(requireActivity(), mediaController)
//            }
//            playPauseBuild()
//            Log.d("MediaPlayer", "Controller Connected")
//        }
//
//        override fun onConnectionFailed() {
//            super.onConnectionFailed()
//            Log.d("MediaPlayer", "Connection Failed")
//
//        }
//    }
//
//
//    private val mControllerCallback = object : MediaControllerCompat.Callback() {
//    }
//
//    fun playPauseBuild() {
//        val mediaController = MediaControllerCompat.getMediaController(requireActivity())
//        btn.setOnClickListener {
//            val state = mediaController.playbackState.state
//            // if it is not playing then what are you waiting for ? PLAY !
//            if (state == PlaybackStateCompat.STATE_PAUSED ||
//                    state == PlaybackStateCompat.STATE_STOPPED ||
//                    state == PlaybackStateCompat.STATE_NONE
//            ) {
//
//                mediaController.transportControls.playFromUri(Uri.parse(GroupAdapter.TEST_MUSIC_URI), null)
//                btn.text = "Pause"
//            }
//            // you are playing ? knock it off !
//            else if (state == PlaybackStateCompat.STATE_PLAYING ||
//                    state == PlaybackStateCompat.STATE_BUFFERING ||
//                    state == PlaybackStateCompat.STATE_CONNECTING
//            ) {
//                mediaController.transportControls.pause()
//                btn.text = "Play"
//            }
//        }
//        mediaController.registerCallback(mControllerCallback)
//
//    }
//
//    fun initializeMediaBrowser() {
//        val componentName = ComponentName(requireContext(), IGMediaService::class.java)
//        // initialize the browser
//        mMediaBrowserCompat = MediaBrowserCompat(
//                requireContext(), componentName, //Identifier for the service
//                connectionCallback,
//                null
//        )
//    }
//
//    fun connectMediaBrowser() {
//        // connect the controllers again to the session
//        // without this connect() you won't be able to start the service neither control it with the controller
//        mMediaBrowserCompat.connect()
//    }
//
//    fun disconnectMediaBrowser() {
//        // Release the resources
//        val controllerCompat = MediaControllerCompat.getMediaController(requireActivity())
//        controllerCompat?.unregisterCallback(mControllerCallback)
//        mMediaBrowserCompat.disconnect()
//    }

//    fun setTestMediaClickListeners() {
//        startService(Intent(this, IGMediaService::class.java).apply {
//            putExtra(VideoService.PLAY_PAUSE_ACTION, 0)
//        })
//    }

    @Inject
    @InjectPresenter
    lateinit var presenter: GroupPresenter

    @ProvidePresenter
    fun providePresenter(): GroupPresenter = presenter

    private lateinit var groupId: String

    private var isGroupCreatedNow = false

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

    lateinit var viewModel: GroupViewModel

    private var mIsTheTitleVisible = false
    private var mIsTheTitleContainerVisible = true

    private var createdPostId: String? = null

    @LayoutRes
    override fun layoutRes() = R.layout.fragment_group

    override fun getSnackBarCoordinator(): CoordinatorLayout = adminGroupCoordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        groupId = arguments?.getString(GROUP_ID)!!
        isGroupCreatedNow = arguments?.getBoolean(IS_GROUP_CREATED_NOW)!!
        viewModel = ViewModelProvider(this, modelFactory)[GroupViewModel::class.java]
        compositeDisposable.add(
                viewModel.fetchPosts(groupId)
                        .subscribe {
                            adapter.submitData(lifecycle, it)
                        }
        )
//        initializeMediaBrowser()
    }

    override fun viewCreated() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>(POST_ID)?.observe(
                viewLifecycleOwner) { id ->
            if (createdPostId != id) {
                createdPostId = id
                adapter.refresh()
            }
        }
        groupPosts.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        groupAvatarHolder.imageLoaderDelegate = imageLoadingDelegate
        GroupPostsAdapter.commentClickListener = { openCommentDetails(InfoForCommentEntity(
                GroupPostEntity(
                        it.id,
                        it.groupInPost,
                        it.postText,
                        it.date,
                        it.updated,
                        it.author,
                        it.unreadComments,
                        it.pin,
                        it.photo,
                        it.commentsCount,
                        it.activeCommentsCount,
                        it.isActive,
                        it.isOffered,
                        it.reacts,
                        it.images,
                        it.audios,
                        it.videos
                )
        )) }
        GroupPostsAdapter.complaintListener = { id -> presenter.complaintPost(id) }
        GroupPostsAdapter.imageClickListener = { list: List<FileEntity>, i: Int ->
            val data = bundleOf("images" to list.toTypedArray(), "selectedId" to i)
            findNavController().navigate(R.id.action_groupActivity_to_imageFragment, data)
        }
        GroupPostsAdapter.likeClickListener = {
            presenter.setReact(isLike = true, isDislike = false, postId = it)
        }
        GroupPostsAdapter.dislikeClickListener = {
            presenter.setReact(isLike = false, isDislike = true, postId = it)
        }
        toolbarBackAction.setOnClickListener { findNavController().popBackStack() }
        appbar.addOnOffsetChangedListener(this)
        presenter.getGroupDetailInfo(groupId)
        groupStrength.setOnClickListener {
            val data = bundleOf(GROUP_ID to groupId)
            findNavController().navigate(R.id.action_groupActivity_to_userListFragment, data)
        }
        newPaging()
    }

    fun newPaging() {
        swipeLayout.setOnRefreshListener {
            adapter.refresh()
        }
        groupPosts.adapter = adapterAD
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
                        loading_layout.gone()
                        swipeLayout.isRefreshing = false
                    }
                    else ->{ swipeLayout.isRefreshing = false }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
//        connectMediaBrowser()
    }

    override fun onResume() {
        super.onResume()
        appbar.addOnOffsetChangedListener(this)
    }

    override fun onStop() {
        appbar.removeOnOffsetChangedListener(this)
        super.onStop()
//        disconnectMediaBrowser()
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

    override fun showGroupInfo(groupEntity: GroupEntity) {
        toolbarTittle.text = groupEntity.name
        //groupName.text = groupEntity.name
        id_group.text = getString(R.string.idg, groupEntity.id)
        likes_count.text = groupEntity.postsLikes
        dislikes_count.text = groupEntity.postsDislikes
        comments_count.text = groupEntity.CommentsCount
        posts_count.text = groupEntity.postsCount
        groupStrength.text = groupEntity.followersCount
        doOrIfNull(groupEntity.avatar, {
            groupAvatarHolder.showAvatar(it)
        }, { groupAvatarHolder.showAvatar(R.drawable.variant_10) })
    }

    override fun showGroupInfoLoading(show: Boolean) {
        if (show) {
            progressBar.show()
        } else {
            progressBar.hide()
        }
    }

    override fun showImageUploadingStarted(path: String) {
        groupAvatarHolder.showImageUploadingStartedWithoutFile()
    }

    override fun showImageUploaded(path: String) {
        presenter.changeGroupAvatar(groupId)
    }

    override fun avatarChanged(url: String) {
        groupAvatarHolder.showAvatar(url)
        groupAvatarHolder.showImageUploaded()
    }

    override fun showImageUploadingProgress(progress: Float, path: String) {
        groupAvatarHolder.showImageUploadingProgress(progress)
    }


    override fun showImageUploadingError(path: String) {
        groupAvatarHolder.clearUploadingState()
    }

    override fun groupFollowed(followersCount: Int) {
        joinToGroup.hide()
        goOutFromGroup.show()
        groupStrength.text = followersCount.toString()
        listenButtonClicks()
        findNavController().previousBackStackEntry?.savedStateHandle?.set(GROUP_ID, GroupInfoEntity(groupId, groupStrength.text.toString(), true))
    }

    override fun groupUnfollowed(followersCount: Int) {
        goOutFromGroup.hide()
        joinToGroup.show()
        groupStrength.text = followersCount.toString()
        listenButtonClicks()
        findNavController().previousBackStackEntry?.savedStateHandle?.set(GROUP_ID, GroupInfoEntity(groupId, groupStrength.text.toString(), false))
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        //super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == Activity.RESULT_OK) {
//            when (requestCode) {
//                COMMENTS_DETAILS_REQUEST -> presenter.refresh(groupId)
//                POST_CREATED -> presenter.refresh(data?.getStringExtra(GROUP_ID_VALUE).orEmpty())
//            }
//        }
//    }

    override fun showMessage(res: Int) {
        Toast.makeText(requireContext(), res, Toast.LENGTH_SHORT).show()
    }

    override fun showMessage(msg: String) {
        //showToast(msg)
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }


    private fun renderAdminPage() {
        //todo разобраться с падением приложения при создании поста в только что созданной группе
        headGroupCreatePostViewStub.inflate()
        createPost.setOnClickListener {
            openCreatePost(groupId)
        }
        groupAvatarHolder.setOnClickListener {
            if (groupAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.UPLOADED
                    || groupAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.NONE) {
                dialogDelegate.showDialog(R.layout.dialog_camera_or_gallery,
                        mapOf(R.id.fromCamera to { presenter.attachFromCamera() }, R.id.fromGallery to { presenter.attachFromGallery() }))
            }
        }
    }

    private fun renderUserPage(viewId: Int) {
        headGroupJoinViewStub.inflate()
        listenButtonClicks()
        requireView().findViewById<TextView>(viewId).show()
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
        //startActivityForResult(CommentsDetailsActivity.getIntent(requireContext(), entity), COMMENTS_DETAILS_REQUEST)
    }

    private fun openCreatePost(post: String) {
        val data = bundleOf(GROUP_ID to post)
        findNavController().navigate(R.id.action_groupActivity_to_CreatePostFragment, data)
        //startActivityForResult(CreatePostFragment.getIntent(requireContext(), post), POST_CREATED)
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
                    presenter.followGroup(groupId, groupStrength.text.toString().split(" ")[0].toInt())
                }.let { { d: Disposable -> compositeDisposable.add(d) } }
        RxView.clicks(goOutFromGroup)
                .take(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    presenter.unfollowGroup(groupId, groupStrength.text.toString().split(" ")[0].toInt())
                }.let { { d: Disposable -> compositeDisposable.add(d) } }
    }

}