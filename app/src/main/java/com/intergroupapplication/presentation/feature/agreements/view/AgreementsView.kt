package com.intergroupapplication.presentation.feature.agreements.view

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import com.intergroupapplication.presentation.base.CanShowLoading

@StateStrategyType(AddToEndSingleStrategy::class)
interface AgreementsView : MvpView, CanShowLoading {
    fun toSplash()
}