package com.intergroupapplication.presentation.delegate

import android.view.View
import android.view.ViewGroup
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.intergroupapplication.presentation.base.BasePagingState
import com.intergroupapplication.presentation.base.PagingView
import com.intergroupapplication.presentation.base.PagingViewGroup
import com.intergroupapplication.presentation.base.adapter.PagingAdapter
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.show
import javax.inject.Inject

class PagingDelegateGroup @Inject constructor() : PagingViewGroup {

    private var adapters: MutableList<PagingAdapter> = arrayListOf()
    private var swipeLayout: ViewGroup? = null
    private var emptyStateViews: MutableList<View> = arrayListOf()

    fun addAdapter(adapter: PagingAdapter, emptyStateView: View) {
        adapters.add(adapter)
        emptyStateViews.add(emptyStateView)
    }

    override fun attachPagingView(swipeLayout: ViewGroup) {
        this.swipeLayout = swipeLayout
    }

    override fun handleState(type: BasePagingState.Type) {
        if (adapters.size>0 && emptyStateViews.size>0) {
            hState(type,adapters[0], emptyStateViews[0])
        }
    }

    override fun handleState1(type: BasePagingState.Type) {
        if (adapters.size>1 && emptyStateViews.size>1) {
            hState(type,adapters[1], emptyStateViews[1])
        }
    }

    override fun handleState2(type: BasePagingState.Type) {
        if (adapters.size>2 && emptyStateViews.size>2) {
            hState(type,adapters[2], emptyStateViews[2])
        }
    }

    fun hState(type: BasePagingState.Type, adapter: PagingAdapter, emptyStateView: View) {
        when (type) {
            BasePagingState.Type.NONE -> {
                adapter.removeLoading()
                adapter.removeError()
                (swipeLayout as? SwipeRefreshLayout)?.isRefreshing = false
                emptyStateView.hide()
                if (adapter.itemCount() == 0) {
                    emptyStateView.show()
                }
            }
            BasePagingState.Type.LOADING -> {
                (swipeLayout as? SwipeRefreshLayout)?.isRefreshing = false
                adapter.removeError()
                emptyStateView.hide()
                adapter.itemCount().let {
                    if (it > 0) {
                        adapter.addLoading()
                    } else {
                        (swipeLayout as? SwipeRefreshLayout)?.isRefreshing = true
                    }
                }
            }
            BasePagingState.Type.ERROR -> {
                (swipeLayout as? SwipeRefreshLayout)?.isRefreshing = false
                emptyStateView.hide()
                adapter.removeLoading()
                adapter.addError()
            }
        }
    }
}