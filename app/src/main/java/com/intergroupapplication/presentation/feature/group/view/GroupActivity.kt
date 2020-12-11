package com.intergroupapplication.presentation.feature.group.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.entity.InfoForCommentEntity
import com.intergroupapplication.domain.entity.UserRole
import com.intergroupapplication.presentation.base.BaseActivity
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.base.BasePresenter.Companion.POST_CREATED
import com.intergroupapplication.presentation.base.ImageUploader
import com.intergroupapplication.presentation.base.PagingView
import com.intergroupapplication.presentation.customview.AvatarImageUploadingView
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.delegate.PagingDelegate
import com.intergroupapplication.presentation.exstension.*
import com.intergroupapplication.presentation.feature.commentsdetails.view.CommentsDetailsActivity
import com.intergroupapplication.presentation.feature.commentsdetails.view.CommentsDetailsActivity.Companion.COMMENTS_DETAILS_REQUEST
import com.intergroupapplication.presentation.feature.commentsdetails.view.CommentsDetailsActivity.Companion.GROUP_ID_VALUE
import com.intergroupapplication.presentation.feature.createpost.view.CreatePostActivity
import com.intergroupapplication.presentation.feature.group.adapter.GroupAdapter
import com.intergroupapplication.presentation.feature.group.presenter.GroupPresenter
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_group.*
import kotlinx.android.synthetic.main.auth_loader.*
import kotlinx.android.synthetic.main.creategroup_toolbar_layout.*
import kotlinx.android.synthetic.main.item_group_header_view.*
import kotlinx.android.synthetic.main.layout_admin_create_post_button.*
import kotlinx.android.synthetic.main.layout_user_join_button.*
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import javax.inject.Inject
import kotlin.math.abs


class GroupActivity(private val pagingDelegate: PagingDelegate) : BaseActivity(), GroupView,
        AppBarLayout.OnOffsetChangedListener, PagingView by pagingDelegate {

    companion object {
        private const val PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR = 0.9f
        private const val PERCENTAGE_TO_HIDE_TITLE_DETAILS = 0.3f
        private const val ALPHA_ANIMATIONS_DURATION = 200
        private const val GROUP_ID = "group_id"

        fun getIntent(context: Context?, groupId: String) = Intent(context, GroupActivity::class.java)
                .apply {
                    action = groupId
                    putExtra(GROUP_ID, groupId)
                }
    }

    constructor() : this(PagingDelegate())

    @Inject
    @InjectPresenter
    lateinit var presenter: GroupPresenter

    @ProvidePresenter
    fun providePresenter(): GroupPresenter = presenter

    @Inject
    override lateinit var navigator: SupportAppNavigator

    @Inject
    lateinit var imageLoadingDelegate: ImageLoadingDelegate

    @Inject
    lateinit var imageUploadingDelegate: ImageUploader

    @Inject
    lateinit var adapter: GroupAdapter

    @Inject
    lateinit var layoutManager: RecyclerView.LayoutManager

    private var mIsTheTitleVisible = false
    private var mIsTheTitleContainerVisible = true

    @LayoutRes
    override fun layoutRes() = R.layout.activity_group

    override fun getSnackBarCoordinator(): CoordinatorLayout = adminGroupCoordinator

    override fun viewCreated() {
        groupPosts.layoutManager = layoutManager
        groupAvatarHolder.imageLoaderDelegate = imageLoadingDelegate
        val groupId = intent.getStringExtra(GROUP_ID) ?: "0"
        adapter.retryClickListener = { presenter.reload() }
        adapter.commentClickListener = { openCommentDetails(InfoForCommentEntity(it)) }
        adapter.complaintListener = { id -> presenter.complaintPost(id) }
        groupPosts.adapter = adapter
        toolbarBackAction.setOnClickListener { presenter.goBack() }
        appbar.addOnOffsetChangedListener(this)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        pagingDelegate.attachPagingView(adapter, swipeLayout, emptyText)
        presenter.getGroupDetailInfo(groupId)
        swipeLayout.setOnRefreshListener {
            presenter.refresh(groupId)
        }
    }

    override fun onResume() {
        super.onResume()
        appbar.addOnOffsetChangedListener(this)
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

    override fun postsLoaded(posts: PagedList<GroupPostEntity>) {
        adapter.submitList(posts)
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

    override fun showImageUploaded() {
        presenter.changeGroupAvatar(intent.getStringExtra(GROUP_ID)!!)
    }

    override fun avatarChanged(url: String) {
        groupAvatarHolder.showAvatar(url)
        groupAvatarHolder.showImageUploaded()
    }

    override fun showImageUploadingProgress(progress: Float) {
        groupAvatarHolder.showImageUploadingProgress(progress)
    }

    override fun showImageUploadingError() {
        groupAvatarHolder.clearUploadingState()
    }

    override fun showLoading(show: Boolean) {
        if (show) {
            emptyText.hide()
            adapter.removeError()
            adapter.addLoading()
        } else {
            swipeLayout.isRefreshing = false
            adapter.removeLoading()
        }
    }

    override fun groupFollowed(followersCount: Int) {
        joinToGroup.hide()
        goOutFromGroup.show()
        groupStrength.text = followersCount.toString()
        listenButtonClicks()
    }

    override fun groupUnfollowed(followersCount: Int) {
        goOutFromGroup.hide()
        joinToGroup.show()
        groupStrength.text = followersCount.toString()
        listenButtonClicks()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                COMMENTS_DETAILS_REQUEST -> presenter.refresh(intent.getStringExtra(GROUP_ID).orEmpty())
                POST_CREATED -> presenter.refresh(data?.getStringExtra(GROUP_ID_VALUE).orEmpty())
            }
        }
    }

    override fun showMessage(res: Int) {
        Toast.makeText(this, res, Toast.LENGTH_SHORT).show()
    }


    private fun renderAdminPage() {
        headGroupCreatePostViewStub.inflate()
        createPost.setOnClickListener {
            openCreatePost(intent.getStringExtra(GROUP_ID)!!)
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
        findViewById<TextView>(viewId).show()
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
        startActivityForResult(CommentsDetailsActivity.getIntent(this, entity), COMMENTS_DETAILS_REQUEST)
    }

    private fun openCreatePost(post: String) {
        startActivityForResult(CreatePostActivity.getIntent(this, post), POST_CREATED)
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
                    presenter.followGroup(intent.getStringExtra(GROUP_ID)!!, groupStrength.text.toString().split(" ")[0].toInt())
                }.let { { d: Disposable -> compositeDisposable.add(d) } }
        RxView.clicks(goOutFromGroup)
                .take(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    presenter.unfollowGroup(intent.getStringExtra(GROUP_ID)!!, groupStrength.text.toString().split(" ")[0].toInt())
                }.let { { d: Disposable -> compositeDisposable.add(d) } }
    }

}
