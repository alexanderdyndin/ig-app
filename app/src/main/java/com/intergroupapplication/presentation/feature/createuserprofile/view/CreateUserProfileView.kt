package com.intergroupapplication.presentation.feature.createuserprofile.view

import com.intergroupapplication.presentation.base.CanShowLoading
import com.intergroupapplication.presentation.base.ImageUploadingView
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface CreateUserProfileView : MvpView, CanShowLoading, ImageUploadingView {
    fun completed()
}
