package com.intergroupapplication.presentation.feature.recoveryPassword.view

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import com.intergroupapplication.presentation.base.CanClearViewErrorState
import com.intergroupapplication.presentation.base.CanShowLoading

@StateStrategyType(AddToEndSingleStrategy::class)
interface RecoveryPasswordView : MvpView, CanShowLoading, CanClearViewErrorState {

    fun showSendEmail(visible: Boolean)

    fun showLoadingSendEmail(value: Boolean)

    fun showLodingCode(load: Boolean)

    fun showPassword(enable: Boolean)

    fun successSaveSetings()

}