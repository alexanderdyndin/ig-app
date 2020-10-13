package com.intergroupapplication.presentation.base

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface ImageUploadingView {
    fun showImageUploadingStarted(path: String)
    fun showImageUploaded()
    fun showImageUploadingProgress(progress: Float)
    fun showImageUploadingError()
}
