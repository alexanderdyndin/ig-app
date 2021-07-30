package com.intergroupapplication.presentation.base

import android.view.View
import com.intergroupapplication.data.model.ChooseMedia
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(SkipStrategy::class)
interface ImageUploadingView {
    fun showImageUploadingStarted(chooseMedia: ChooseMedia = ChooseMedia(""))
    fun showImageUploaded(path: String = "")
    fun showImageUploadingProgress(progress: Float, path: String = "")
    fun showImageUploadingError(path: String = "")
}
