package com.intergroupapplication.presentation.feature.registration.view

import com.intergroupapplication.presentation.base.CanClearViewErrorState
import com.intergroupapplication.presentation.base.CanShowLoading
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface RegistrationView : MvpView, CanShowLoading, CanClearViewErrorState {
    fun deviceInfoExtracted()
    fun confirmMail(email: String)
}

