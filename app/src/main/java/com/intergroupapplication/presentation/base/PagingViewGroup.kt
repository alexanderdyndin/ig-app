package com.intergroupapplication.presentation.base

import android.view.View
import android.view.ViewGroup
import com.intergroupapplication.presentation.base.adapter.PagingAdapter
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface PagingViewGroup {
    fun attachPagingView(swipeLayout: ViewGroup)

    fun handleState(type: BasePagingState.Type)
    fun handleState1(type: BasePagingState.Type)
    fun handleState2(type: BasePagingState.Type)
}