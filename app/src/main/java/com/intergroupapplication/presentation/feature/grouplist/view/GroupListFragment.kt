package com.intergroupapplication.presentation.feature.grouplist.view

import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import com.appodeal.ads.Appodeal
import com.clockbyte.admobadapter.bannerads.AdmobBannerRecyclerAdapterWrapper
import com.google.android.material.tabs.TabLayoutMediator
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.intergroupapplication.R
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.domain.entity.GroupInfoEntity
import com.intergroupapplication.domain.entity.GroupType
import com.intergroupapplication.domain.entity.UserEntity
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.customview.AvatarImageUploadingView
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.doOrIfNull
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.ExitActivity
import com.intergroupapplication.presentation.feature.grouplist.adapter.GroupListAdapter3
import com.intergroupapplication.presentation.feature.grouplist.adapter.GroupListsAdapter
import com.intergroupapplication.presentation.feature.grouplist.other.GroupsFragment
import com.intergroupapplication.presentation.feature.grouplist.other.ViewPager2Circular
import com.intergroupapplication.presentation.feature.grouplist.presenter.GroupListPresenter
import com.intergroupapplication.presentation.feature.grouplist.viewModel.GroupListViewModel
import com.intergroupapplication.presentation.feature.mainActivity.view.MainActivity
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_group_list.*
import kotlinx.android.synthetic.main.item_group_in_list.view.*
import kotlinx.android.synthetic.main.layout_profile_header.view.*
import kotlinx.android.synthetic.main.main_toolbar_layout.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class GroupListFragment(): BaseFragment(), GroupListView {


    companion object {
        const val CREATED_GROUP_ID = "created_group_id"
    }

    @Inject
    @InjectPresenter
    lateinit var presenter: GroupListPresenter

    @ProvidePresenter
    fun providePresenter(): GroupListPresenter = presenter

    @Inject
    lateinit var sessionStorage: UserSession

    @Inject
    lateinit var imageLoadingDelegate: ImageLoadingDelegate

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: GroupListViewModel

    private var exitHandler: Handler? = null

    private var doubleBackToExitPressedOnce = false

    val exitFlag = Runnable { this.doubleBackToExitPressedOnce = false }

    private lateinit var viewDrawer: View

    lateinit var drawer: Drawer

    private lateinit var profileAvatarHolder: AvatarImageUploadingView

    var groupInfoEntity: GroupInfoEntity? = null

    var groupId: String? = null

    var currentScreen = 0

    @Inject
    @Named("all")
    lateinit var adapterAll: GroupListAdapter3

    @Inject
    @Named("subscribed")
    lateinit var adapterSubscribed: GroupListAdapter3

    @Inject
    @Named("owned")
    lateinit var adapterOwned: GroupListAdapter3

    @Inject
    @Named("all")
    lateinit var adapterAllAd: AdmobBannerRecyclerAdapterWrapper

    @Inject
    @Named("subscribed")
    lateinit var adapterSubscribedAd: AdmobBannerRecyclerAdapterWrapper

    @Inject
    @Named("owned")
    lateinit var adapterOwnedAd: AdmobBannerRecyclerAdapterWrapper


    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            fetchGroups(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    }

    override fun layoutRes() = R.layout.fragment_group_list

    override fun getSnackBarCoordinator(): ViewGroup? = groupListCoordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), modelFactory)[GroupListViewModel::class.java]
        fetchGroups()
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    ExitActivity.exitApplication(requireContext())
                    return
                }
                doubleBackToExitPressedOnce = true
                Toast.makeText(requireContext(), getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show()
                exitHandler = Handler(Looper.getMainLooper())
                exitHandler?.postDelayed(exitFlag, MainActivity.EXIT_DELAY)
            }
        })
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Appodeal.cache(requireActivity(), Appodeal.NATIVE, 10)

        val adapterList: MutableList<AdmobBannerRecyclerAdapterWrapper> = mutableListOf()
        adapterList.add(adapterAllAd)
        adapterList.add(adapterSubscribedAd)
        adapterList.add(adapterOwnedAd)

        pager.apply {
            adapter = GroupListsAdapter(adapterList)
            val handler = ViewPager2Circular(this, swipe_groups)
            handler.pageChanged = {
                currentScreen = it
            }
            registerOnPageChangeCallback(handler)
            (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }

        val tabTitles = arrayOf(resources.getString(R.string.allGroups), resources.getString(R.string.subGroups), resources.getString(R.string.admGroups))
        TabLayoutMediator(slidingCategories, pager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        activity_main__btn_filter.setOnClickListener {
            //todo
        }
        createGroup.visibility = View.VISIBLE
        createGroup.setOnClickListener { openCreateGroup() }
        with (GroupListAdapter3) {
            userID = sessionStorage.user?.id
            groupClickListener = {
                val data = bundleOf(GROUP_ID to it)
                findNavController().navigate(R.id.action_groupListFragment2_to_groupActivity, data)
            }
            subscribeClickListener = { group, view ->
                compositeDisposable.add(viewModel.subscribeGroup(group.id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe {
                            view.subscribingProgressBar.show()
                            view.item_group__text_sub.isEnabled = false
                        }
                        .doOnComplete {
                            view.subscribingProgressBar.hide()
                            group.isFollowing = true
                            with(view.item_group__text_sub){
                                isEnabled = true
                            }
                            when (currentScreen) {
                                0 -> {
                                    adapterSubscribed.refresh()
                                    adapterAll.notifyDataSetChanged()
                                }
                                1 -> {
                                    adapterAll.refresh()
                                    adapterSubscribed.notifyDataSetChanged()
                                }
                            }
                        }
                        .doOnError {
                            view.subscribingProgressBar.hide()
                            view.item_group__text_sub.isEnabled = true
                        }
                        .subscribe({ }, {
                            errorHandler.handle(it)
                        }))
            }
            unsubscribeClickListener = { group, view ->
                compositeDisposable.add(viewModel.unsubscribeGroup(group.id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe {
                            view.subscribingProgressBar.show()
                            view.item_group__text_sub.isEnabled = false
                        }
                        .doOnComplete {
                            view.subscribingProgressBar.hide()
                            group.isFollowing = false
                            with(view.item_group__text_sub){
                                isEnabled = true
                            }
                            when (currentScreen) {
                                0 -> {
                                    adapterSubscribed.refresh()
                                    adapterAll.notifyDataSetChanged()
                                }
                                1 -> {
                                    adapterAll.refresh()
                                    adapterSubscribed.notifyDataSetChanged()
                                }
                            }
                        }
                        .doOnError {
                            view.subscribingProgressBar.hide()
                            view.item_group__text_sub.isEnabled = true
                        }
                        .subscribe({ }, {
                            errorHandler.handle(it)
                        }))
            }
        }
        swipe_groups.setOnRefreshListener {
            when (currentScreen) {
                0 -> adapterAll.refresh()
                1 -> adapterSubscribed.refresh()
                2-> adapterOwned.refresh()
            }
        }
        lifecycleScope.launch {
            adapterAll.loadStateFlow.collectLatest { loadStates ->
                when (loadStates.refresh) {
                    is LoadState.Loading -> {
                        adapterAll.removeError()
                        //adapterAll.addLoading()
                        //                    if (adapterGroups.itemCount == 0)
                        //                        progress_loading.show()
                        //                    else adapterGroups.addLoading()
                        //                    adapterGroups.removeError()
                        //                    emptyText.hide()
                    }
                    is LoadState.Error -> {
                        swipe_groups.isRefreshing = false
                        adapterAll.addError()
                        //                    emptyText.hide()
                        //                    adapterGroups.addError()
                        //                    adapterGroups.removeLoading()
                        //                    progress_loading.hide()
                        errorHandler.handle((loadStates.refresh as LoadState.Error).error)
                    }
                    is LoadState.NotLoading -> {
                        //                    if (adapterGroups.itemCount == 0) {
                        //                        emptyText.show()
                        //                    } else {
                        //                        emptyText.hide()
                        //                    }
                        //                    adapterGroups.removeError()
                        //                    adapterGroups.removeLoading()
                        //                    group_swipe.isRefreshing = false
                        //                    progress_loading.hide()
                        adapterAll.removeError()
                        swipe_groups.isRefreshing = false
                    }
                }
            }
        }
        lifecycleScope.launch {
            adapterSubscribed.loadStateFlow.collectLatest { loadStates ->
                when (loadStates.refresh) {
                    is LoadState.Loading -> {
                        adapterSubscribed.removeError()
                    }
                    is LoadState.Error -> {
                        adapterSubscribed.addError()
                        swipe_groups.isRefreshing = false
                        errorHandler.handle((loadStates.refresh as LoadState.Error).error)
                    }
                    is LoadState.NotLoading -> {
                        adapterSubscribed.removeError()
                        swipe_groups.isRefreshing = false
                    }
                }
            }
        }
        lifecycleScope.launch {
            adapterOwned.loadStateFlow.collectLatest { loadStates ->
                when (loadStates.refresh) {
                    is LoadState.Loading -> {
                        adapterOwned.removeError()
                    }
                    is LoadState.Error -> {
                        adapterOwned.addError()
                        swipe_groups.isRefreshing = false
                        errorHandler.handle((loadStates.refresh as LoadState.Error).error)
                    }
                    is LoadState.NotLoading -> {
                        adapterOwned.removeError()
                        swipe_groups.isRefreshing = false
                    }
                }
            }
        }
    }

    fun fetchGroups(query: String = "") {
        compositeDisposable.clear()
        compositeDisposable.add(
                viewModel.fetchGroups(query)
                        .subscribe {
                            adapterAll.submitData(lifecycle, it)
                        }
        )
        compositeDisposable.add(
                viewModel.fetchSubGroups(query)
                        .subscribe {
                            adapterSubscribed.submitData(lifecycle, it)
                        }
        )
        compositeDisposable.add(
                viewModel.fetchAdmGroups(query)
                        .subscribe {
                            adapterOwned.submitData(lifecycle, it)
                        }
        )
    }


    override fun onResume() {
        super.onResume()
        activity_main__search_input.addTextChangedListener(textWatcher)
    }

    override fun onPause() {
        super.onPause()
        activity_main__search_input.removeTextChangedListener(textWatcher)
    }

    private fun openCreateGroup() {
        findNavController().navigate(R.id.action_groupListFragment2_to_createGroupActivity)
    }


//    private inner class GroupPageAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
//
//        private val PAGE_COUNT = 3
//
//        override fun getItemCount(): Int = PAGE_COUNT
//
//        override fun createFragment(position: Int): Fragment {
//            return when(position) {
//                0 -> GroupsFragment(GroupType.ALL)
//                1 -> GroupsFragment(GroupType.FOLLOWED)
//                2 -> GroupsFragment(GroupType.OWNED)
//                else -> GroupsFragment(GroupType.ALL)
//            }
//        }
//
//    }


    override fun showImageUploadingStarted(path: String) {
        //profileAvatarHolder.showImageUploadingStarted(path)
        profileAvatarHolder.showImageUploadingStartedWithoutFile()
    }

    override fun showImageUploaded(path: String) {
        presenter.changeUserAvatar()
    }



    override fun avatarChanged(url: String) {
        profileAvatarHolder.showAvatar(url)
        profileAvatarHolder.showImageUploaded()
    }

    override fun showLastAvatar(lastAvatar: String?) {
        profileAvatarHolder.clearUploadingState(lastAvatar)
    }


    override fun showImageUploadingProgress(progress: Float, path: String) {
        profileAvatarHolder.showImageUploadingProgress(progress)
    }


    override fun showImageUploadingError(path: String) {
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
            primaryItem(getString(R.string.news)) {
                icon = R.drawable.ic_news
                selectedIcon = R.drawable.ic_news_blue
                textColorRes = R.color.whiteTextColor
                selectedColorRes = R.color.profileTabColor
                selectedTextColorRes = R.color.selectedItemTabColor
                typeface = Typeface.createFromAsset(requireActivity().assets, "roboto.regular.ttf")
                onClick { v ->
                    findNavController().navigate(R.id.action_groupListFragment2_to_newsFragment2)
                    toolbarTittle.text = getString(R.string.news)
                    false
                }
            }
            drawerItem = primaryItem(getString(R.string.groups)) {
                icon = R.drawable.ic_groups
                selectedIcon = R.drawable.ic_groups_blue
                textColorRes = R.color.whiteTextColor
                selectedColorRes = R.color.profileTabColor
                selectedTextColorRes = R.color.selectedItemTabColor
                typeface = Typeface.createFromAsset(requireActivity().assets, "roboto.regular.ttf")
            }
            primaryItem(getString(R.string.logout)) {
                typeface = Typeface.createFromAsset(requireActivity().assets, "roboto.regular.ttf")
                textColorRes = R.color.whiteTextColor
                selectedColorRes = R.color.profileTabColor
                selectedTextColorRes = R.color.selectedItemTabColor
                onClick { v ->
                    presenter.goOutFromProfile()
                    findNavController().navigate(R.id.action_groupListFragment2_to_loginActivity2)
                    false
                }
            }
        }.apply {
            setSelection(drawerItem)
            drawerItem.withOnDrawerItemClickListener { _, _, _ ->
                findNavController().navigate(R.id.action_groupListFragment2_self)
                toolbarTittle.text = getString(R.string.groups)
                false
            }
            viewDrawer.drawerArrow.setOnClickListener { closeDrawer() }
        }
        toolbarMenu.setOnClickListener {
            drawer.openDrawer()
        }
        presenter.getUserInfo()
    }

}
