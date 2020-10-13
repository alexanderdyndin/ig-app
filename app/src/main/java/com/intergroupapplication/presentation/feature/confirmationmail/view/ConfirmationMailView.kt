package com.intergroupapplication.presentation.feature.confirmationmail.view

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import com.intergroupapplication.domain.entity.RegistrationEntity
import com.intergroupapplication.presentation.base.CanClearViewErrorState
import com.intergroupapplication.presentation.base.CanShowError
import com.intergroupapplication.presentation.base.CanShowLoading

@StateStrategyType(AddToEndSingleStrategy::class)
interface ConfirmationMailView : MvpView, CanShowLoading, CanClearViewErrorState {
    fun showMessage(resId: Int)
    fun fillData(email: String)
}

