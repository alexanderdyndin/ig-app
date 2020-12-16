package com.intergroupapplication.presentation.feature.news.view

import android.annotation.SuppressLint
import android.app.Activity
import androidx.paging.PagedList
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import com.appodeal.ads.Appodeal
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.clockbyte.admobadapter.bannerads.AdmobBannerRecyclerAdapterWrapper
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.entity.InfoForCommentEntity
import com.intergroupapplication.domain.entity.UserEntity
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.base.PagingView
import com.intergroupapplication.presentation.customview.AvatarImageUploadingView
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.delegate.PagingDelegate
import com.intergroupapplication.presentation.exstension.doOrIfNull
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.feature.commentsdetails.view.CommentsDetailsActivity
import com.intergroupapplication.presentation.feature.commentsdetails.view.CommentsDetailsActivity.Companion.COMMENTS_COUNT_VALUE
import com.intergroupapplication.presentation.feature.commentsdetails.view.CommentsDetailsActivity.Companion.COMMENTS_DETAILS_REQUEST
import com.intergroupapplication.presentation.feature.commentsdetails.view.CommentsDetailsActivity.Companion.GROUP_ID_VALUE
import com.intergroupapplication.presentation.feature.news.adapter.NewsAdapter
import com.intergroupapplication.presentation.feature.news.presenter.NewsPresenter
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import kotlinx.android.synthetic.main.activity_navigation.*
import kotlinx.android.synthetic.main.fragment_news.*
import kotlinx.android.synthetic.main.layout_profile_header.view.*
import kotlinx.android.synthetic.main.main_toolbar_layout.*
import javax.inject.Inject

class NewsFragment @SuppressLint("ValidFragment") constructor(private val pagingDelegate: PagingDelegate)
    : BaseFragment(), NewsView,
        PagingView by pagingDelegate {

    constructor() : this(PagingDelegate())

    companion object {

        fun getInstance() = NewsFragment()

    }

    @Inject
    @InjectPresenter
    lateinit var presenter: NewsPresenter

    @ProvidePresenter
    fun providePresenter(): NewsPresenter = presenter

    @Inject
    lateinit var adapter: NewsAdapter

    @Inject
    lateinit var imageLoadingDelegate: ImageLoadingDelegate

    @Inject
    lateinit var diffUtil: DiffUtil.ItemCallback<GroupPostEntity>

    @Inject
    lateinit var layoutManager: RecyclerView.LayoutManager

    @Inject
    lateinit var adapterWrapper: AdmobBannerRecyclerAdapterWrapper

    override fun layoutRes() = R.layout.fragment_news
    override fun getSnackBarCoordinator(): ViewGroup? = newsCoordinator

    private lateinit var viewDrawer: View

    lateinit var drawer: Drawer

    private lateinit var profileAvatarHolder: AvatarImageUploadingView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Appodeal.cache(requireActivity(), Appodeal.NATIVE, 5)
        pagingDelegate.attachPagingView(adapter, newSwipe, emptyText)
        newsPosts.layoutManager = layoutManager
        newsPosts.itemAnimator = null
        adapter.retryClickListener = { presenter.reload() }
        adapter.commentClickListener = { openCommentDerails(InfoForCommentEntity(it, true)) }
        adapter.groupClickListener = {
            val data = bundleOf("groupId" to it)
            findNavController().navigate(R.id.action_newsFragment2_to_groupActivity, data)
        }
        adapter.complaintListener = { presenter.complaintPost(it) }
        newsPosts.adapter = adapterWrapper
        newSwipe.setOnRefreshListener { presenter.refresh()
            val t = Appodeal.getNativeAds(2)}
        //presenter.getNews()
        //(activity as NavigationActivity).newsLaunchCount++
    }


    override fun onResume() {
        super.onResume()
        presenter.checkNewVersionAvaliable(activity?.supportFragmentManager!!)
        presenter.refresh()
    }


    override fun newsLoaded(posts: PagedList<GroupPostEntity>) {
        adapter.submitList(posts)
    }

    override fun showMessage(resId: Int) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()
    }

    override fun showLoading(show: Boolean) {
        if (show) {
            emptyText.hide()
            adapter.removeError()
            //TODO ПОФИКСИТЬ ПЕРЕСКАКИВАНИЕ В КОНЕЦ СПИСКА ПРИ ПОВТОРНОЙ ЗАГРУЗКЕ ФРАГМЕНТА
            //if ((activity as NavigationActivity).newsLaunchCount == 1) {
                adapter.addLoading()
            //}
            //newsPosts.visibility = View.INVISIBLE
            //progressBar.visibility = View.VISIBLE
        } else {
            newSwipe.isRefreshing = false
            adapter.removeLoading()
            //progressBar.visibility = View.INVISIBLE
            //newsPosts.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                COMMENTS_DETAILS_REQUEST -> {
                    val groupId = data?.getStringExtra(GROUP_ID_VALUE).orEmpty()
                    val commentCount = data?.getStringExtra(COMMENTS_COUNT_VALUE).orEmpty()
                    adapter.itemUpdate(groupId, commentCount)
                }
            }
        }
    }

    override fun onDestroy() {
        adapterWrapper.release()
        super.onDestroy()
    }

    fun openCommentDerails(entity: InfoForCommentEntity) {
        startActivityForResult(CommentsDetailsActivity.getIntent(context, entity), COMMENTS_DETAILS_REQUEST)
    }

    override fun showImageUploadingStarted(path: String) {
        //profileAvatarHolder.showImageUploadingStarted(path)
        profileAvatarHolder.showImageUploadingStartedWithoutFile()
    }

    override fun showImageUploaded() {
        presenter.changeUserAvatar()
    }


    override fun avatarChanged(url: String) {
        profileAvatarHolder.showAvatar(url)
        profileAvatarHolder.showImageUploaded()
    }

    override fun showLastAvatar(lastAvatar: String?) {
        profileAvatarHolder.clearUploadingState(lastAvatar)
    }

    override fun showImageUploadingProgress(progress: Float) {
        profileAvatarHolder.showImageUploadingProgress(progress)
    }

    override fun showImageUploadingError() {
        profileAvatarHolder.clearUploadingState()
        presenter.showLastUserAvatar()
    }

    override fun showUserInfo(userEntity: UserEntity) {
        val userName = userEntity.firstName + " " + userEntity.surName
        viewDrawer.profileName.text = userName
        doOrIfNull(userEntity.avatar,
                { profileAvatarHolder.showAvatar(it) },
                { profileAvatarHolder.showAvatar(R.drawable.application_logo) })
    }

    override fun viewCreated() {
        viewDrawer = layoutInflater.inflate(R.layout.layout_profile_header, navigationCoordinator, false)
        profileAvatarHolder = viewDrawer.profileAvatarHolder
        profileAvatarHolder.imageLoaderDelegate = imageLoadingDelegate
        lateinit var drawerItem: PrimaryDrawerItem
        drawer = drawer {
            sliderBackgroundColorRes = R.color.profileTabColor
            headerView = viewDrawer
            actionBarDrawerToggleEnabled = true
            translucentStatusBar = true
            viewDrawer.profileAvatarHolder.setOnClickListener {
                if (profileAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.UPLOADED
                        || profileAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.NONE) {
                    dialogDelegate.showDialog(R.layout.dialog_camera_or_gallery,
                            mapOf(R.id.fromCamera to { presenter.attachFromCamera() }, R.id.fromGallery to { presenter.attachFromGallery() }))
                }
            }
            drawerItem = primaryItem(getString(R.string.news)) {
                icon = R.drawable.ic_news
                selectedIcon = R.drawable.ic_news_blue
                textColorRes = R.color.whiteTextColor
                selectedColorRes = R.color.profileTabColor
                selectedTextColorRes = R.color.selectedItemTabColor
                typeface = Typeface.createFromAsset(requireActivity().assets, "roboto.regular.ttf")
                onClick { v ->
                   // v?.findNavController()?.navigate(R.id.action_groupListFragment2_to_newsFragment2)
                    toolbarTittle.text = getString(R.string.news)
                    false
                }
            }
            primaryItem(getString(R.string.groups)) {
                icon = R.drawable.ic_groups
                selectedIcon = R.drawable.ic_groups_blue
                textColorRes = R.color.whiteTextColor
                selectedColorRes = R.color.profileTabColor
                selectedTextColorRes = R.color.selectedItemTabColor
                typeface = Typeface.createFromAsset(requireActivity().assets, "roboto.regular.ttf")
                onClick { v ->
                    //presenter.goToGroupListScreen()
                    findNavController().navigate(R.id.action_newsFragment2_to_groupListFragment2)
                    toolbarTittle.text = getString(R.string.groups)
                    false
                }
            }
            primaryItem(getString(R.string.logout)) {
                typeface = Typeface.createFromAsset(requireActivity().assets, "roboto.regular.ttf")
                textColorRes = R.color.whiteTextColor
                selectedColorRes = R.color.profileTabColor
                selectedTextColorRes = R.color.selectedItemTabColor
                onClick { v ->
                    presenter.goOutFromProfile()
                    findNavController().navigate(R.id.action_newsFragment2_to_splashActivity)
                    toolbarTittle.text = getString(R.string.logout)
                    false
                }
            }
        }.apply {
            setSelection(drawerItem)
        }
        toolbarMenu.setOnClickListener {
            drawer.openDrawer()
        }
        presenter.getUserInfo()
    }
}
