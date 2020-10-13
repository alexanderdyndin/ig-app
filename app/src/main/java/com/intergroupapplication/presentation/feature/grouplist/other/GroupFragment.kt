package com.intergroupapplication.presentation.feature.grouplist.other

import android.annotation.SuppressLint
import androidx.paging.PagedList
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.presentation.base.BasePagingState
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.base.PagingView
import com.intergroupapplication.presentation.base.adapter.PagingAdapter
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.delegate.PagingDelegate
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.feature.creategroup.view.CreateGroupActivity
import com.intergroupapplication.presentation.feature.group.view.GroupScreen
import com.intergroupapplication.presentation.feature.grouplist.adapter.GroupListAdapter
import com.intergroupapplication.presentation.feature.grouplist.pagingsource.GroupListDataSourceFactory
import com.intergroupapplication.presentation.feature.grouplist.view.GroupListView
import com.workable.errorhandler.ErrorHandler
import dagger.android.AndroidInjection.inject
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_group_category.*
import kotlinx.android.synthetic.main.fragment_group_list.*
import ru.terrakok.cicerone.Router
import javax.inject.Inject


@SuppressLint("ValidFragment")
open class GroupsFragment constructor(
        protected val viewState:GroupListView,
        private val pagingDelegate: PagingDelegate
) : Fragment(), GroupListView, PagingView by pagingDelegate {

    @Inject
    lateinit var errorHandler: ErrorHandler

    @Inject
    lateinit var imageLoadingDelegate: ImageLoadingDelegate

    @Inject
    lateinit var diffUtil: DiffUtil.ItemCallback<GroupEntity>

    private var mPage = 0

    protected val groupsDisposable = CompositeDisposable()

    protected lateinit var adapter:GroupListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mPage = arguments!!.getInt(AllGroupsFragment.ARG_PAGE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_group_category, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getGroupsList()
        val groupsList = view.findViewById<RecyclerView>(R.id.allGroupsList)
        val emptyText = view.findViewById<TextView>(R.id.emptyText)
        groupsList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        groupsList.setHasFixedSize(true)
        groupsList.itemAnimator = null

        adapter =  GroupListAdapter(diffUtil, imageLoadingDelegate)
        adapter.retryClickListener = { reload() }
        adapter.groupClickListener = { goToGroupScreen(it) }
        groupsList.adapter = adapter
        createGroup.setOnClickListener { openCreateGroup() }
        swipeLayout.setOnRefreshListener { getGroupsList() }
        pagingDelegate.attachPagingView(adapter, swipeLayout, emptyText)
    }

    override fun onDestroy() {
        super.onDestroy()
        groupsDisposable.clear()
    }

    override fun groupListLoaded(groups: PagedList<GroupEntity>) {
        adapter.submitList(groups)
        spanLetters(searchField.text.toString())
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

    override fun attachPagingView(adapter: PagingAdapter, swipeLayout: ViewGroup, emptyStateView: View) {
    }

    override fun handleState(type: BasePagingState.Type) {
    }

    open fun getGroupsList() {
    }

    fun applySearchQuery(searchQuery:String){
        //groupListDataSourceFactory?.source?.applySearchFilter(searchQuery)
        refresh()
    }

    fun reload() {
        //groupListDataSourceFactory?.source?.reload()
    }

    fun refresh() {
        unsubscribe()
        getGroupsList()
    }

    fun goToGroupScreen(groupId: String) {
        //router.navigateTo(GroupScreen(groupId))
    }

    private fun spanLetters(textToSpan: String){
        GroupListAdapter.lettersToSpan = textToSpan
    }

    private fun unsubscribe() {
        groupsDisposable.clear()
    }

    private fun openCreateGroup() {
        startActivityForResult(CreateGroupActivity.getIntent(context), BasePresenter.GROUP_CREATED)
    }

}