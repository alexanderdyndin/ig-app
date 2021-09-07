package com.intergroupapplication.presentation.feature.confirmationmail.view

import com.intergroupapplication.presentation.base.CanClearViewErrorState
import com.intergroupapplication.presentation.base.CanShowLoading
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface ConfirmationMailView : MvpView, CanShowLoading, CanClearViewErrorState {
    fun showMessage(resId: Int)
    fun fillData(email: String)
    fun completed()
}
