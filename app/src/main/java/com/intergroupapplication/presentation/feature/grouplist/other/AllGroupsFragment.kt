package com.intergroupapplication.presentation.feature.grouplist.other

import androidx.paging.PagedList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import moxy.viewstate.MvpViewState
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.presentation.base.BasePagingState
import com.intergroupapplication.presentation.base.adapter.PagingAdapter
import com.intergroupapplication.presentation.delegate.PagingDelegate
import com.intergroupapplication.presentation.feature.grouplist.adapter.GroupListAdapter
import com.intergroupapplication.presentation.feature.grouplist.view.GroupListView
import kotlinx.android.synthetic.main.fragment_group_list.*

class AllGroupsFragment: Fragment(), GroupListView {
    private var mPage = 0
    var doOnViewCreated:(View) -> Unit = {}
    lateinit var adapter: GroupListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mPage = arguments!!.getInt(ARG_PAGE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_group_category, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        doOnViewCreated(view)
    }

    companion object {
        const val ARG_PAGE = "ARG_PAGE"
        fun newInstance(page: Int): AllGroupsFragment {
            val args = Bundle()
            args.putInt(ARG_PAGE, page)
            val fragment = AllGroupsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun groupListLoaded(groups: PagedList<GroupEntity>) {
        adapter.submitList(groups)
        spanLetters(searchField.text.toString())
    }

    override fun showLoading(show: Boolean) {
        if (show) {
//            emptyText.hide()
            adapter.removeError()
            adapter.addLoading()
        } else {
            swipeLayout.isRefreshing = false
            adapter.removeLoading()
        }
    }

    private fun spanLetters(textToSpan: String){
        GroupListAdapter.lettersToSpan = textToSpan
    }


    override fun attachPagingView(adapter: PagingAdapter, swipeLayout: ViewGroup, emptyStateView: View) {
        TODO("Not yet implemented")
    }

    override fun handleState(type: BasePagingState.Type) {
        TODO("Not yet implemented")
    }


}