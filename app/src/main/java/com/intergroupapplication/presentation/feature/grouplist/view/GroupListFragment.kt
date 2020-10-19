package com.intergroupapplication.presentation.feature.grouplist.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
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
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.delegate.PagingDelegate
import com.intergroupapplication.presentation.feature.creategroup.view.CreateGroupActivity
import com.intergroupapplication.presentation.feature.grouplist.adapter.GroupListAdapter
import com.intergroupapplication.presentation.feature.grouplist.other.GroupPageAdapter
import com.intergroupapplication.presentation.feature.grouplist.other.ViewPager2Circular
import com.intergroupapplication.presentation.feature.grouplist.presenter.GroupListPresenter
import kotlinx.android.synthetic.main.fragment_group_list.*
import javax.inject.Inject

class GroupListFragment @SuppressLint("ValidFragment") constructor(private val pagingDelegate: PagingDelegate)
    : BaseFragment(), GroupListView, PagingView by pagingDelegate {

    constructor() : this(PagingDelegate())

    companion object {
        fun getInstance() = GroupListFragment()
    }

    private val pagingAll = PagingDelegate()
//    private lateinit var pagingSub: PagingDelegate
//    private lateinit var pagingAdm: PagingDelegate

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

    private val tabTitles = arrayOf("Все группы", "Подписки", "Управление")

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
        val gpAdapter = GroupPageAdapter(this, adapterAll, adapterSub, adapterAdm)
        gpAdapter.doOnFragmentViewCreated = { it ->
            val emptyText = it.findViewById<TextView>(R.id.emptyText)
            val groupsList = it.findViewById<RecyclerView>(R.id.allGroupsList)
            groupsList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            groupsList.setHasFixedSize(true)
            groupsList.itemAnimator = null
            pagingDelegate.attachPagingView(adapterAll, swipeLayout, emptyText)
        }
        pager.apply {
            adapter = gpAdapter
            val handler = ViewPager2Circular(this)
            handler.pageChanged = {
                when (it) {
                    0 -> pagingDelegate.changeAdapter(adapterAll)
                    1 -> pagingDelegate.changeAdapter(adapterSub)
                    2 -> pagingDelegate.changeAdapter(adapterAdm)
                    else-> pagingDelegate.changeAdapter(adapterAll)
                }
            }
            registerOnPageChangeCallback(handler)
            (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
        TabLayoutMediator(slidingCategories, pager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
        createGroup.setOnClickListener { openCreateGroup() }
        swipeLayout.setOnRefreshListener { presenter.groupList() }
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
        searchField.addTextChangedListener(textWatcher)
    }

    override fun onPause() {
        super.onPause()
        searchField.removeTextChangedListener(textWatcher)
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
