package com.intergroupapplication.presentation.feature.grouplist.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toolbar
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import co.zsmb.materialdrawerkt.builders.drawer
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import moxy.MvpView
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import com.intergroupapplication.R
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.base.BasePresenter.Companion.GROUP_CREATED
import com.intergroupapplication.presentation.base.PagingView
import com.intergroupapplication.presentation.base.PagingViewGroup
import com.intergroupapplication.presentation.base.adapter.PagingAdapter
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.delegate.PagingDelegate
import com.intergroupapplication.presentation.delegate.PagingDelegateGroup
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.feature.creategroup.view.CreateGroupActivity
import com.intergroupapplication.presentation.feature.grouplist.adapter.GroupListAdapter
import com.intergroupapplication.presentation.feature.grouplist.other.GroupPageAdapter
import com.intergroupapplication.presentation.feature.grouplist.other.ViewPager2Circular
import com.intergroupapplication.presentation.feature.grouplist.presenter.GroupListPresenter
import com.intergroupapplication.presentation.feature.navigation.view.NavigationActivity
import kotlinx.android.synthetic.main.activity_navigation.*
import kotlinx.android.synthetic.main.fragment_group_list.*
import kotlinx.android.synthetic.main.main_toolbar_layout.view.*
import javax.inject.Inject

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

    @Inject
    lateinit var diffUtil: DiffUtil.ItemCallback<GroupEntity>


    //адаптеры для фрагментов сос списками групп
    lateinit var adapterAll: GroupListAdapter
    lateinit var adapterSub: GroupListAdapter
    lateinit var adapterAdm: GroupListAdapter

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
        adapterAll = setAdapter()
        adapterSub = setAdapter()
        adapterAdm = setAdapter()
        pagingDelegate.attachPagingView(swipeLayout)
        val gpAdapter = GroupPageAdapter(this, adapterAll, adapterSub, adapterAdm)
        gpAdapter.doOnFragmentViewCreated = { v ->
            val emptyText = v.findViewById<TextView>(R.id.emptyText)
            val groupsList = v.findViewById<RecyclerView>(R.id.allGroupsList)
            pagingDelegate.addAdapter(groupsList.adapter as PagingAdapter, emptyText)
        }
        pager.apply {
            adapter = gpAdapter
            val handler = ViewPager2Circular(this, swipeLayout)
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
    }

    private fun setAdapter(): GroupListAdapter {
        val a = GroupListAdapter(diffUtil, imageLoadingDelegate, sessionStorage.user?.id)
        with (a) {
            retryClickListener = { presenter.reload() }
            groupClickListener = { presenter.goToGroupScreen(it) }
            subscribeClickListener = { presenter.sub(it)}
            unsubscribeClickListener = { presenter.unsub(it) }
        }
        return a
    }

    override fun onResume() {
        super.onResume()
        presenter.checkNewVersionAvaliable(fragmentManager!!)
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

    }
}
