package com.intergroupapplication.presentation.feature.news.view

import android.graphics.Typeface
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.intergroupapplication.presentation.customview.AvatarImageUploadingView
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.doOrIfNull
import com.intergroupapplication.presentation.exstension.gone
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.group.di.GroupViewModule
import com.intergroupapplication.presentation.feature.news.adapter.NewsAdapter3
import com.intergroupapplication.presentation.feature.news.presenter.NewsPresenter
import com.intergroupapplication.presentation.feature.news.viewmodel.NewsViewModel
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.workable.errorhandler.Action
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_news.*
import kotlinx.android.synthetic.main.layout_profile_header.view.*
import kotlinx.android.synthetic.main.main_toolbar_layout.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

class NewsFragment(): BaseFragment(), NewsView{

    @Inject
    @InjectPresenter
    lateinit var presenter: NewsPresenter

    @ProvidePresenter
    fun providePresenter(): NewsPresenter = presenter
    
    @Inject
    lateinit var imageLoadingDelegate: ImageLoadingDelegate

    @Inject
    lateinit var diffUtil: DiffUtil.ItemCallback<GroupPostEntity>

    @Inject
    lateinit var layoutManager: RecyclerView.LayoutManager

    @Inject
    lateinit var adapterWrapper: AdmobBannerRecyclerAdapterWrapper

    @Inject
    lateinit var adapterNews: NewsAdapter3

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: NewsViewModel

    override fun layoutRes() = R.layout.fragment_news

    override fun getSnackBarCoordinator(): ViewGroup? = newsCoordinator

    private lateinit var viewDrawer: View

    lateinit var drawer: Drawer

    lateinit var profileAvatarHolder: AvatarImageUploadingView

    var clickedPostId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, modelFactory)[NewsViewModel::class.java]
        adapterNews.complaintListener = { presenter.complaintPost(it) }
        adapterNews.commentClickListener = {
            clickedPostId = it.id
            openCommentDetails(InfoForCommentEntity(it, true)) }
        adapterNews.groupClickListener = {
            val data = bundleOf(GROUP_ID to it)
            findNavController().navigate(R.id.action_newsFragment2_to_groupActivity, data)
        }
        compositeDisposable.add(
                viewModel.getNews()
                        .subscribe {
                            adapterNews.submitData(lifecycle, it)
                        }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Appodeal.cache(requireActivity(), Appodeal.NATIVE, 5)
        
        //paging 3
        newPaging()
        //crashing app when provide it by dagger
        //newsPosts.layoutManager = layoutManager
        newsPosts.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        newsPosts.itemAnimator = null

    }

    fun newPaging() {
        newSwipe.setOnRefreshListener {
            adapterNews.refresh()
        }
        newsPosts.adapter = adapterWrapper
        lifecycleScope.launch {
            adapterNews.loadStateFlow.collectLatest { loadStates ->
                when(loadStates.refresh) {
                    is LoadState.Loading -> {
                        if (adapterNews.itemCount == 0) {
                            loading_layout.show()
                        } else adapterNews.addLoading()
                        adapterNews.removeError()
                        emptyText.hide()
                    }
                    is LoadState.Error -> {
                        newSwipe.isRefreshing = false
                        emptyText.hide()
                        adapterNews.removeLoading()
                        loading_layout.gone()
                        adapterNews.addError()
                        errorHandler.handle((loadStates.refresh as LoadState.Error).error)
                    }
                    is LoadState.NotLoading -> {
                        if (adapterNews.itemCount == 0) {
                            emptyText.show()
                        } else {
                            emptyText.hide()
                        }
                        loading_layout.gone()
                        adapterNews.removeError()
                        adapterNews.removeLoading()
                        newSwipe.isRefreshing = false
                        //if (!job.isCancelled) progress_first_loading.hide()
                    }
                    else ->{ newSwipe.isRefreshing = false } //if (!job.isCancelled) progress_first_loading.hide()
                }
            }
        }
    }

//    fun oldPaging() {
//        pagingDelegate.attachPagingView(adapter, newSwipe, emptyText)
//        adapter.retryClickListener = { presenter.reload() }
//        adapter.commentClickListener = {
//            clickedPostId = it.id
//            openCommentDetails(InfoForCommentEntity(it, true)) }
//        adapter.groupClickListener = {
//            val data = bundleOf(GROUP_ID to it)
//            findNavController().navigate(R.id.action_newsFragment2_to_groupActivity, data)
//        }
//        adapter.complaintListener = { presenter.complaintPost(it) }
//        newsPosts.adapter = adapterWrapper
//        newSwipe.setOnRefreshListener { presenter.refresh() }
//        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>(COMMENTS_COUNT_VALUE)?.observe(
//                viewLifecycleOwner) { commentCount ->
//            adapter.itemUpdate(clickedPostId.orEmpty(), commentCount)
//        }
//    }


    override fun showMessage(resId: Int) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        adapterWrapper.release()
        presenter.unsubscribe()
        super.onDestroy()
    }

    fun openCommentDetails(entity: InfoForCommentEntity) {
        val data = bundleOf(GroupViewModule.COMMENT_POST_ENTITY to entity)
        findNavController().navigate(R.id.action_newsFragment2_to_commentsDetailsActivity, data)
    }


    override fun viewCreated() {
        viewDrawer = layoutInflater.inflate(R.layout.layout_profile_header, navigationCoordinator, false)
        viewDrawer.profileAvatarHolder.setOnClickListener {
                if (profileAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.UPLOADED
                        || profileAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.NONE) {
                    dialogDelegate.showDialog(R.layout.dialog_camera_or_gallery,
                            mapOf(R.id.fromCamera to { presenter.attachFromCamera() }, R.id.fromGallery to { presenter.attachFromGallery() }))
                }
            }
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
                    userSession.logout()
                    findNavController().navigate(R.id.action_newsFragment2_to_loginActivity)
                    false
                }
            }
        }.apply {
            setSelection(drawerItem)
            viewDrawer.drawerArrow.setOnClickListener { closeDrawer() }
            drawerItem.withOnDrawerItemClickListener { _, _, _ ->
                findNavController().navigate(R.id.action_newsFragment2_self)
                toolbarTittle.text = getString(R.string.news)
                false
            }
        }
        toolbarMenu.setOnClickListener {
            drawer.openDrawer()
        }
        presenter.getUserInfo()
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

    override fun openConfirmationEmail() = Action { _, _ ->
        val email = userSession.email?.email.orEmpty()
        val data = bundleOf("entity" to email)
        findNavController().navigate(R.id.action_newsFragment2_to_confirmationMailActivity, data)
    }

    override fun openCreateProfile()  = Action { _, _ ->
        findNavController().navigate(R.id.action_newsFragment2_to_createUserProfileActivity)
    }

}
