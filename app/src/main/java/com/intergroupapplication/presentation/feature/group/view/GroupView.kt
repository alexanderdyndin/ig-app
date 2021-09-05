package com.intergroupapplication.presentation.feature.group.view

import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.domain.entity.UserRole
import com.intergroupapplication.presentation.base.ImageUploadingView
import moxy.MvpView
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(SkipStrategy::class)
interface GroupView : MvpView, ImageUploadingView {
    fun renderViewByRole(userRole: UserRole)
    fun showGroupInfo(groupEntity: GroupEntity.Group)
    fun groupFollowed(followersCount: Int)
    fun groupUnfollowed(followersCount: Int)
    fun showSubscribeLoading(show: Boolean)
    fun groupFollowedError()
    fun groupUnfollowedError()
    fun showGroupInfoLoading(show: Boolean)
    fun changeCommentsCount(pair: Pair<String, String>)
    fun avatarChanged(url: String)
    fun showMessage(res:Int)
    fun showMessage(msg: String)
}