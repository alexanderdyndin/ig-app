package com.intergroupapplication.presentation.base

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

/**
 * Created by abakarmagomedov on 23/08/2018 at project InterGroupApplication.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface CanClearViewErrorState {
    fun clearViewErrorState()
}