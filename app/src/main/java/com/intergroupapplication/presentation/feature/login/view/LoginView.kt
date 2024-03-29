package com.intergroupapplication.presentation.feature.login.view

import com.intergroupapplication.presentation.base.CanClearViewErrorState
import com.intergroupapplication.presentation.base.CanShowLoading
import moxy.MvpView
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(SkipStrategy::class)
interface LoginView : MvpView, CanShowLoading, CanClearViewErrorState {
    fun deviceInfoExtracted()
    fun login()
}
