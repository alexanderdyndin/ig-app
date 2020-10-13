package com.intergroupapplication.presentation.feature.registration.view

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import com.intergroupapplication.presentation.base.CanClearViewErrorState
import com.intergroupapplication.presentation.base.CanShowError
import com.intergroupapplication.presentation.base.CanShowLoading

@StateStrategyType(AddToEndSingleStrategy::class)
interface RegistrationView : MvpView, CanShowLoading, CanClearViewErrorState {
    fun deviceInfoExtracted()
}

