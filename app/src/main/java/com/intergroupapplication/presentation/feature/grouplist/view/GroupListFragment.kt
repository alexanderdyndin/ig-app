package com.intergroupapplication.presentation.feature.grouplist.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.domain.entity.UserEntity
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.base.BasePresenter.Companion.GROUP_CREATED
import com.intergroupapplication.presentation.base.PagingViewGroup
import com.intergroupapplication.presentation.base.adapter.PagingAdapter
import com.intergroupapplication.presentation.customview.AvatarImageUploadingView
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.delegate.PagingDelegateGroup
import com.intergroupapplication.presentation.exstension.doOrIfNull
import com.intergroupapplication.presentation.feature.grouplist.adapter.GroupListAdapter
import com.intergroupapplication.presentation.feature.grouplist.other.GroupsFragment
import com.intergroupapplication.presentation.feature.grouplist.other.ViewPager2Circular
import com.intergroupapplication.presentation.feature.grouplist.presenter.GroupListPresenter
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.auth_loader.*
import kotlinx.android.synthetic.main.fragment_group_list.*
import kotlinx.android.synthetic.main.fragment_news.*
import kotlinx.android.synthetic.main.layout_profile_header.*
import kotlinx.android.synthetic.main.layout_profile_header.view.*
import kotlinx.android.synthetic.main.main_toolbar_layout.*
import kotlinx.android.synthetic.main.main_toolbar_layout.view.*
import javax.inject.Inject
import javax.inject.Named

class GroupListFragment @SuppressLint("ValidFragment") constructor(private val pagingDelegate: PagingDelegateGroup)
    : BaseFragment(), GroupListView, PagingViewGroup by pagingDelegate {

    constructor() : this(PagingDelegateGroup())

    companion object {
        fun getInstance() = GroupListFragment()
    }

    @Inject
    @InjectPresenter
    lateinit var presenter: GroupListPresenter

    @ProvidePresenter
    fun providePresenter(): GroupListPresenter = presenter

    @Inject
    lateinit var sessionStorage: UserSession

    @Inject
    lateinit var layoutManager: LinearLayoutManager

    @Inject
    lateinit var imageLoadingDelegate: ImageLoadingDelegate

    lateinit var doOnFragmentViewCreated: (View) -> Unit

    private lateinit var viewDrawer: View

    lateinit var drawer: Drawer

    private lateinit var profileAvatarHolder: AvatarImageUploadingView

    //адаптеры для фрагментов со списками групп
    @Inject
    @Named("AdapterAll")
    lateinit var adapterAll: GroupListAdapter

    @Inject
    @Named("AdapterSub")
    lateinit var adapterSub: GroupListAdapter

    @Inject
    @Named("AdapterAdm")
    lateinit var adapterAdm: GroupListAdapter

    @Inject
    @Named("AdapterAll")
    lateinit var adapterAllAD: AdmobBannerRecyclerAdapterWrapper

    @Inject
    @Named("AdapterSub")
    lateinit var adapterSubAD: AdmobBannerRecyclerAdapterWrapper

    @Inject
    @Named("AdapterAdm")
    lateinit var adapterAdmAD: AdmobBannerRecyclerAdapterWrapper

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            presenter.applySearchQuery(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    }

    override fun layoutRes() = R.layout.fragment_group_list

    override fun getSnackBarCoordinator(): ViewGroup? = groupListCoordinator


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Appodeal.cache(requireActivity(), Appodeal.NATIVE, 10)
        setAdapter()
        pagingDelegate.attachPagingView(swipeLayout)
        doOnFragmentViewCreated = {
            val emptyText = it.findViewById<TextView>(R.id.emptyText)
            val groupsList = it.findViewById<RecyclerView>(R.id.allGroupsList)
            pagingDelegate.addAdapter((groupsList.adapter as AdmobBannerRecyclerAdapterWrapper).adapter as PagingAdapter, emptyText)
        }
        val gpAdapter = GroupPageAdapter(requireActivity())
        pager.apply {
            adapter = gpAdapter
            val handler = ViewPager2Circular(this, swipeLayout)
            handler.pageChanged = {
                presenter.currentScreen = it
                //presenter.groupList()
            }
            registerOnPageChangeCallback(handler)
            (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
        val tabTitles = arrayOf(resources.getString(R.string.allGroups), resources.getString(R.string.subGroups), resources.getString(R.string.admGroups))
        TabLayoutMediator(slidingCategories, pager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
        swipeLayout.setOnRefreshListener {
            presenter.groupList()
            Appodeal.cache(requireActivity(), Appodeal.NATIVE, 10)
        }
        activity_main__btn_filter.setOnClickListener {
            //todo
        }
        createGroup.visibility = View.VISIBLE
        createGroup.setOnClickListener { openCreateGroup() }
    }

    private fun setAdapter() {
        with (GroupListAdapter) {
            userID = sessionStorage.user?.id
            retryClickListener = { presenter.reload() }
            subscribeClickListener = { presenter.sub(it) }
            unsubscribeClickListener = { presenter.unsub(it) }
            groupClickListener = {
                val data = bundleOf(GROUP_ID to it)
                findNavController().navigate(R.id.action_groupListFragment2_to_groupActivity, data)
            }
            getColor = { ContextCompat.getColor(this@GroupListFragment.requireContext(), R.color.whiteTextColor) }
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.checkNewVersionAvaliable(requireActivity().supportFragmentManager)
        activity_main__search_input.addTextChangedListener(textWatcher)
    }

    override fun onPause() {
        super.onPause()
        activity_main__search_input.removeTextChangedListener(textWatcher)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //super.onActivityResult(requestCode, resultCode, data)
        //todo добавить livedata из фрагмента создания группы
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GROUP_CREATED -> presenter.refresh()
            }
        }
    }

    private fun openCreateGroup() {
        //startActivityForResult(CreateGroupActivity.getIntent(context), GROUP_CREATED)
        findNavController().navigate(R.id.action_groupListFragment2_to_createGroupActivity)
    }

    override fun groupListLoaded(groups: PagedList<GroupEntity>) {
        adapterAll.submitList(groups)
}

    override fun groupListSubLoaded(groups: PagedList<GroupEntity>) {
        adapterSub.submitList(groups)
    }

    override fun groupListAdmLoaded(groups: PagedList<GroupEntity>) {
        adapterAdm.submitList(groups)
    }

    override fun showLoading(show: Boolean) {
        //TODO починить отсутствие получения статуса false для showloading
        if (show) {
//            when (pager.currentItem) {
//                0 -> {
//                    adapterAll.removeError()
//                    adapterAll.addLoading()
//                }
//                1 -> {
//                    adapterAdm.removeError()
//                    adapterAdm.addLoading()
//                }
//                2 -> {
//                    adapterSub.removeError()
//                    adapterSub.addLoading()
//                }
            //}
            //pager.visibility = View.INVISIBLE
            //progressBar.visibility = View.VISIBLE
        } else {
            swipeLayout.isRefreshing = false
//            when (pager.currentItem) {
//                0 -> {
//                    adapterAll.removeLoading()
//                }
//                1 -> {
//                    adapterAdm.removeLoading()
//                }
//                2 -> {
//                    adapterSub.removeLoading()
//                }
//            }
            //pager.visibility = View.VISIBLE
            //progressBar.visibility = View.INVISIBLE
        }
    }

    private inner class GroupPageAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

        private val PAGE_COUNT = 3


        override fun getItemCount(): Int = PAGE_COUNT

        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> GroupsFragment.newInstance(position, adapterAllAD)
                        .apply { doOnViewCreated = doOnFragmentViewCreated }
                1 -> GroupsFragment.newInstance(position, adapterSubAD)
                        .apply { doOnViewCreated = doOnFragmentViewCreated }
                2 -> GroupsFragment.newInstance(position, adapterAdmAD)
                        .apply { doOnViewCreated = doOnFragmentViewCreated }
                else -> GroupsFragment.newInstance(position, adapterAllAD)
                        .apply { doOnViewCreated = doOnFragmentViewCreated }
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

    override fun subscribeGroup(id: String) {
        adapterAll.itemUpdate(id)
        presenter.getFollowGroupsList()
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
        //findNavController().navigate(R.id.action_navigationActivity2_to_newsFragment2)
        //drawer.openDrawer()
    }

}
