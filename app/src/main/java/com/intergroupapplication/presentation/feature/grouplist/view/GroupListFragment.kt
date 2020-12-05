package com.intergroupapplication.presentation.feature.grouplist.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.appodeal.ads.Appodeal
import com.clockbyte.admobadapter.bannerads.AdmobBannerRecyclerAdapterWrapper
import com.google.android.material.tabs.TabLayoutMediator
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.intergroupapplication.R
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.base.BasePresenter.Companion.GROUP_CREATED
import com.intergroupapplication.presentation.base.PagingViewGroup
import com.intergroupapplication.presentation.base.adapter.PagingAdapter
import com.intergroupapplication.presentation.delegate.PagingDelegateGroup
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.feature.creategroup.view.CreateGroupActivity
import com.intergroupapplication.presentation.feature.grouplist.adapter.GroupListAdapter
import com.intergroupapplication.presentation.feature.grouplist.other.GroupsFragment
import com.intergroupapplication.presentation.feature.grouplist.other.ViewPager2Circular
import com.intergroupapplication.presentation.feature.grouplist.presenter.GroupListPresenter
import com.intergroupapplication.presentation.feature.navigation.view.NavigationActivity
import kotlinx.android.synthetic.main.activity_navigation.*
import kotlinx.android.synthetic.main.auth_loader.*
import kotlinx.android.synthetic.main.fragment_group_list.*
import kotlinx.android.synthetic.main.fragment_news.*
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

    lateinit var doOnFragmentViewCreated: (View) -> Unit



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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Appodeal.cache(requireActivity(), Appodeal.NATIVE, 5)
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
        swipeLayout.setOnRefreshListener { presenter.groupList() }
//        activity_main__btn_search.setOnClickListener {
//            activity_main__search_input.requestFocus()
//            if (activity is NavigationActivity) {
//                val imm: InputMethodManager = (activity as NavigationActivity).getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
//                imm.showSoftInput(this.view, InputMethodManager.SHOW_IMPLICIT)
//            }
//        }
        activity_main__btn_filter.setOnClickListener {
            //todo
        }
        if (activity is NavigationActivity) {
            with (activity as NavigationActivity) {
                navigationToolbar.activity_main__text_created_group.visibility = View.VISIBLE
                navigationToolbar.activity_main__text_created_group.setOnClickListener { openCreateGroup() }
            }
        }
        //presenter.groupList()
    }

    private fun setAdapter() {
        with (GroupListAdapter) {
            userID = sessionStorage.user?.id
            retryClickListener = { presenter.reload() }
            groupClickListener = { presenter.goToGroupScreen(it) }
            subscribeClickListener = { presenter.sub(it)}
            unsubscribeClickListener = { presenter.unsub(it) }
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


    override fun onDestroyView() {
        super.onDestroyView()
        if (activity is NavigationActivity) {
            (activity as NavigationActivity).navigationToolbar.activity_main__text_created_group.visibility = View.INVISIBLE
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GROUP_CREATED -> presenter.refresh()
            }
        }
    }

    private fun openCreateGroup() {
        startActivityForResult(CreateGroupActivity.getIntent(context), GROUP_CREATED)
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
}
