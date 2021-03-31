package com.intergroupapplication.presentation.base

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface VideoUploadingView {
    fun showVideoUploadingStarted(path: String = "")
    fun showVideoUploaded(path: String = "")
    fun showVideoUploadingProgress(progress: Float, path: String = "")
    fun showVideoUploadingError(path: String = "")
}
