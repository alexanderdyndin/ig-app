package com.intergroupapplication.presentation.feature.grouplist.view

import androidx.paging.PagedList
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.presentation.base.CanShowLoading
import com.intergroupapplication.presentation.base.PagingView
import com.intergroupapplication.presentation.base.PagingViewGroup
import com.intergroupapplication.presentation.feature.grouplist.adapter.GroupListAdapter

@StateStrategyType(AddToEndSingleStrategy::class)
interface GroupListView : MvpView, CanShowLoading, PagingViewGroup {
    fun groupListLoaded(groups: PagedList<GroupEntity>)
    fun groupListSubLoaded(groups: PagedList<GroupEntity>)
    fun groupListAdmLoaded(groups: PagedList<GroupEntity>)
}