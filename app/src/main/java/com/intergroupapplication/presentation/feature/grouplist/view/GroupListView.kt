package com.intergroupapplication.presentation.feature.grouplist.view

import moxy.MvpView
import moxy.viewstate.strategy.StateStrategyType
import com.intergroupapplication.domain.entity.UserEntity
import com.intergroupapplication.presentation.base.ImageUploadingView
import moxy.viewstate.strategy.SkipStrategy

@StateStrategyType(SkipStrategy::class)
interface GroupListView : MvpView, ImageUploadingView {
    fun showUserInfo(userEntity: UserEntity)
    fun avatarChanged(url: String)
    fun showLastAvatar(lastAvatar: String?)
}