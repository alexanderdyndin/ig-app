package com.intergroupapplication.presentation.base

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface AudioUploadingView {
    fun showAudioUploadingStarted(path: String = "")
    fun showAudioUploaded(path: String = "")
    fun showAudioUploadingProgress(progress: Float, path: String = "")
    fun showAudioUploadingError(path: String = "")
}
