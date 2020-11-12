package com.intergroupapplication.presentation.delegate

import android.view.View
import android.view.ViewGroup
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.intergroupapplication.presentation.base.BasePagingState
import com.intergroupapplication.presentation.base.PagingView
import com.intergroupapplication.presentation.base.adapter.PagingAdapter
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.show
import javax.inject.Inject

class PagingDelegate @Inject constructor() : PagingView {

    private var adapter: PagingAdapter? = null
    private var swipeLayout: ViewGroup? = null
    private var emptyStateView: View? = null


    override fun attachPagingView(adapter: PagingAdapter, swipeLayout: ViewGroup, emptyStateView: View) {
        this.adapter = adapter
        this.swipeLayout = swipeLayout
        this.emptyStateView = emptyStateView
    }

    override fun handleState(type: BasePagingState.Type) {
        when (type) {
            BasePagingState.Type.NONE -> {
                adapter?.removeLoading()
                adapter?.removeError()
                (swipeLayout as? SwipeRefreshLayout)?.isRefreshing = false
                emptyStateView?.hide()
                if (adapter?.itemCount() == 0) {
                    emptyStateView?.show()
                }
            }
            BasePagingState.Type.LOADING -> {
                (swipeLayout as? SwipeRefreshLayout)?.isRefreshing = false
                adapter?.removeError()
                emptyStateView?.hide()
                adapter?.itemCount()?.let {
                    if (it  > 0) {
                        adapter?.addLoading()
                    } else {
                        (swipeLayout as? SwipeRefreshLayout)?.isRefreshing = true
                    }
                }
            }
            BasePagingState.Type.ERROR -> {
                (swipeLayout as? SwipeRefreshLayout)?.isRefreshing = false
                emptyStateView?.hide()
                adapter?.removeLoading()
                adapter?.addError()
            }
        }
    }
}