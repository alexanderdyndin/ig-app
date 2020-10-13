package com.intergroupapplication.presentation.base

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

/**
 * Created by abakarmagomedov on 31/07/2018 at project InterGroupApplication.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface CanShowLoading {
    fun showLoading(show: Boolean)
}
