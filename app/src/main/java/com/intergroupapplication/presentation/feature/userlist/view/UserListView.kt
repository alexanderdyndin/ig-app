package com.intergroupapplication.presentation.feature.userlist.view

import androidx.paging.PagedList
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.presentation.base.CanShowLoading
import com.intergroupapplication.presentation.base.PagingViewGroup
import moxy.MvpView
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(SkipStrategy::class)
interface UserListView : MvpView, CanShowLoading, PagingViewGroup {
    fun userListLoaded(users: PagedList<GroupEntity>)
    fun userListAdmLoaded(users: PagedList<GroupEntity>)
}