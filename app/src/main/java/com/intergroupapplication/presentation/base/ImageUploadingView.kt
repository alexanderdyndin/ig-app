package com.intergroupapplication.presentation.base

import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(SkipStrategy::class)
interface ImageUploadingView {
    fun showImageUploadingStarted(path: String = "")
    fun showImageUploaded(path: String = "")
    fun showImageUploadingProgress(progress: Float, path: String = "")
    fun showImageUploadingError(path: String = "")
}
