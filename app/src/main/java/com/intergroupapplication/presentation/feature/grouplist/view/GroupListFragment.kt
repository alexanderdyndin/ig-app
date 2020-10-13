package com.intergroupapplication.presentation.feature.grouplist.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
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
import com.intergroupapplication.presentation.feature.grouplist.other.CircularViewPagerHandler
import com.intergroupapplication.presentation.feature.grouplist.other.SampleFragmentPagerAdapter
import com.intergroupapplication.presentation.feature.grouplist.presenter.GroupListPresenter
import kotlinx.android.synthetic.main.fragment_group_list.*
import javax.inject.Inject
import javax.inject.Named

class GroupListFragment @SuppressLint("ValidFragment") constructor(private val pagingDelegate: PagingDelegate)
    : BaseFragment(), GroupListView, PagingView by pagingDelegate {

    constructor() : this(PagingDelegate())

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
    lateinit var adapter: GroupListAdapter

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
        // Получаем ViewPager и устанавливаем в него адаптер
        val viewPager: ViewPager = view.findViewById(R.id.viewpager)
        val pagerAdapter = SampleFragmentPagerAdapter(activity!!.supportFragmentManager)
        pagerAdapter.doOnFragmentViewCreated = { it ->
            val groupsList = it.findViewById<RecyclerView>(R.id.allGroupsList)
            val emptyText = it.findViewById<TextView>(R.id.emptyText)
            groupsList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            groupsList.setHasFixedSize(true)
            groupsList.itemAnimator = null
            adapter.retryClickListener = { presenter.reload() }
            adapter.groupClickListener = { presenter.goToGroupScreen(it) }
            adapter.subscribeClickListener = { presenter.sub(it)}
            adapter.unsubscribeClickListener = { presenter.unsub(it) }
            groupsList.adapter = adapter
            pagingDelegate.attachPagingView(adapter, swipeLayout, emptyText)
        }
        viewPager.adapter = pagerAdapter
        val handler = CircularViewPagerHandler(viewPager)
        handler.pageChanged = {
            presenter.groupList(it)
        }
        viewPager.addOnPageChangeListener(handler)
        // Передаём ViewPager в TabLayout
        val tabLayout: TabLayout = view.findViewById(R.id.slidingCategories)
        tabLayout.setupWithViewPager(viewPager)
        createGroup.setOnClickListener { openCreateGroup() }
        swipeLayout.setOnRefreshListener { presenter.groupList() }
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

    private fun spanLetters(textToSpan: String){
        GroupListAdapter.lettersToSpan = textToSpan
    }

    private fun openCreateGroup() {
        startActivityForResult(CreateGroupActivity.getIntent(context), GROUP_CREATED)
    }

    override fun groupListLoaded(groups: PagedList<GroupEntity>) {
        adapter.submitList(groups)
}

    override fun showLoading(show: Boolean) {

    }
}
