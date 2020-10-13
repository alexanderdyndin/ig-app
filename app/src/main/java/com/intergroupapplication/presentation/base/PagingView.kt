package com.intergroupapplication.presentation.base

import android.view.View
import android.view.ViewGroup
import com.intergroupapplication.presentation.base.adapter.PagingAdapter

interface PagingView {
    fun attachPagingView(adapter: PagingAdapter,
                         swipeLayout: ViewGroup,
                         emptyStateView: View)

    fun handleState(type: BasePagingState.Type)
}