package com.intergroupapplication.presentation.feature.commentsdetails.view

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface BottomSheetView: MvpView {
}