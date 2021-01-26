package com.intergroupapplication.presentation.feature.grouplist.view

import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import com.appodeal.ads.Appodeal
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
import com.intergroupapplication.presentation.feature.grouplist.adapter.GroupListAdapter3
import com.intergroupapplication.presentation.feature.grouplist.other.GroupsFragment
import com.intergroupapplication.presentation.feature.grouplist.other.ViewPager2Circular
import com.intergroupapplication.presentation.feature.grouplist.presenter.GroupListPresenter
import com.intergroupapplication.presentation.feature.grouplist.viewModel.GroupListViewModel
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_group_list.*
import kotlinx.android.synthetic.main.layout_profile_header.view.*
import kotlinx.android.synthetic.main.main_toolbar_layout.*
import javax.inject.Inject

class GroupListFragment()
    : BaseFragment(), GroupListView {


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

    //lateinit var doOnFragmentViewCreated: (View) -> Unit

    private lateinit var viewDrawer: View

    lateinit var drawer: Drawer

    private lateinit var profileAvatarHolder: AvatarImageUploadingView

    var groupInfoEntity: GroupInfoEntity? = null

    var groupId: String? = null

    var currentScreen = 0


    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            //presenter.applySearchQuery(s.toString())
            viewModel.query.value = s.toString()
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    }

    override fun layoutRes() = R.layout.fragment_group_list

    override fun getSnackBarCoordinator(): ViewGroup? = groupListCoordinator


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), modelFactory)[GroupListViewModel::class.java]
        viewModel.query.value = activity_main__search_input.text.toString()
        Appodeal.cache(requireActivity(), Appodeal.NATIVE, 10)
        val gpAdapter = GroupPageAdapter(requireActivity())
        pager.apply {
            adapter = gpAdapter
            val handler = ViewPager2Circular(this/*, swipeLayout*/)
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

        viewModel.scrollingRecycler.observe(viewLifecycleOwner, Observer {
            pager.isUserInputEnabled = it
        })
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
        }
    }

//    fun setPagingOld() {
        //pagingDelegate.attachPagingView(swipeLayout)
//        doOnFragmentViewCreated = {
//            val emptyText = it.findViewById<TextView>(R.id.emptyText)
//            val groupsList = it.findViewById<RecyclerView>(R.id.allGroupsList)
//            pagingDelegate.addAdapter((groupsList.adapter as AdmobBannerRecyclerAdapterWrapper).adapter as PagingAdapter, emptyText)
//        }
//        with (GroupListAdapter) {
//            userID = sessionStorage.user?.id
//            retryClickListener = { presenter.reload() }
//
//            unsubscribeClickListener = { id, view ->
//                presenter.unsub(id, view)
//            }
//            groupClickListener = {
//                val data = bundleOf(GROUP_ID to it)
//                findNavController().navigate(R.id.action_groupListFragment2_to_groupActivity, data)
//            }
//            getColor = { ContextCompat.getColor(this@GroupListFragment.requireContext(), R.color.whiteTextColor) }
//        }
//        swipeLayout.setOnRefreshListener {
//            when(currentScreen) {
//                0 -> presenter.refreshAll()
//                1 -> presenter.refreshFollowed()
//                2 -> presenter.refreshAdmin()
//            }
//            Appodeal.cache(requireActivity(), Appodeal.NATIVE, 10)
//        }
//        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<GroupInfoEntity>(GROUP_ID)?.observe(
//                viewLifecycleOwner) { groupInfo ->
//            if (groupInfoEntity != groupInfo) { // avoid get same info on ever fragment start
//                adapterAll.itemUpdate(groupInfo)
//                presenter.refreshFollowed()
//                groupInfoEntity = groupInfo
//            }
            //presenter.refresh()
//        }
//        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>(CREATED_GROUP_ID)?.observe(
//                viewLifecycleOwner) { id ->
//            if (groupId != id) { // avoid get same info on ever fragment start
//                val data = bundleOf(GROUP_ID to id)
//                findNavController().navigate(R.id.action_groupListFragment2_to_groupActivity, data)
//                groupId = id
//                presenter.refresh()
//            }
//        }
//    }


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


    private inner class GroupPageAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

        private val PAGE_COUNT = 3

        override fun getItemCount(): Int = PAGE_COUNT

        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> GroupsFragment(GroupType.ALL)
                1 -> GroupsFragment(GroupType.FOLLOWED)
                2 -> GroupsFragment(GroupType.OWNED)
                else -> GroupsFragment(GroupType.ALL)
            }
        }

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
