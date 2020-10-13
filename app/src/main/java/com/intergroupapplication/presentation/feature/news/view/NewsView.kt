package com.intergroupapplication.presentation.feature.news.view

import androidx.paging.PagedList
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.presentation.base.BasePagingState
import com.intergroupapplication.presentation.base.CanShowLoading
import com.intergroupapplication.presentation.base.PagingView

@StateStrategyType(AddToEndSingleStrategy::class)
interface NewsView : MvpView, CanShowLoading, PagingView {
    fun newsLoaded(posts: PagedList<GroupPostEntity>)
    fun showMessage(resId: Int)
}
