package com.intergroupapplication.presentation.feature.login.view

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import com.intergroupapplication.presentation.base.CanClearViewErrorState
import com.intergroupapplication.presentation.base.CanShowLoading

@StateStrategyType(AddToEndSingleStrategy::class)
interface LoginView : MvpView, CanShowLoading, CanClearViewErrorState {
    fun deviceInfoExtracted()
    fun login()
}
