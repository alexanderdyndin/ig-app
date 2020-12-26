package com.intergroupapplication.presentation.feature.grouplist.view

import androidx.paging.PagedList
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.domain.entity.UserEntity
import com.intergroupapplication.presentation.base.CanShowLoading
import com.intergroupapplication.presentation.base.ImageUploadingView
import com.intergroupapplication.presentation.base.PagingView
import com.intergroupapplication.presentation.base.PagingViewGroup
import com.intergroupapplication.presentation.feature.grouplist.adapter.GroupListAdapter
import moxy.viewstate.strategy.SkipStrategy

@StateStrategyType(SkipStrategy::class)
interface GroupListView : MvpView, CanShowLoading, PagingViewGroup, ImageUploadingView {
    fun groupListLoaded(groups: PagedList<GroupEntity>)
    fun groupListSubLoaded(groups: PagedList<GroupEntity>)
    fun groupListAdmLoaded(groups: PagedList<GroupEntity>)
    fun showUserInfo(userEntity: UserEntity)
    fun avatarChanged(url: String)
    fun showLastAvatar(lastAvatar: String?)
    fun subscribeGroup(id: String)
}