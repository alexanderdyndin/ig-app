package com.intergroupapplication.presentation.feature.editpostbottomsheet.view

import com.intergroupapplication.presentation.base.ImageUploadingView
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
@StateStrategyType(AddToEndSingleStrategy::class)
interface EditPostBottomSheetView  : MvpView, ImageUploadingView