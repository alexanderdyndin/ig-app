package com.intergroupapplication.presentation.feature.login.view

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import com.intergroupapplication.presentation.base.CanClearViewErrorState
import com.intergroupapplication.presentation.base.CanShowLoading
import moxy.viewstate.strategy.SkipStrategy

@StateStrategyType(SkipStrategy::class)
interface LoginView : MvpView, CanShowLoading, CanClearViewErrorState {
    fun deviceInfoExtracted()
    fun login()
}
