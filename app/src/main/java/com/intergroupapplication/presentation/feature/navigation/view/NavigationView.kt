package com.intergroupapplication.presentation.feature.navigation.view

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import com.intergroupapplication.domain.entity.UserEntity
import com.intergroupapplication.presentation.base.ImageUploadingView

@StateStrategyType(AddToEndSingleStrategy::class)
interface NavigationView : MvpView, ImageUploadingView {
    fun showUserInfo(userEntity: UserEntity)
    fun avatarChanged(url: String)
    fun showLastAvatar(lastAvatar: String?)
}