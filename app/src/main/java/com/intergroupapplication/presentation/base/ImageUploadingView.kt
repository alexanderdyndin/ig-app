package com.intergroupapplication.presentation.base

import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.domain.entity.MediaType
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(SkipStrategy::class)
interface ImageUploadingView {
    fun showImageUploadingStarted(
        chooseMedia: ChooseMedia =
            ChooseMedia("", name = "", type = MediaType.IMAGE)
    )

    fun showImageUploaded(
        chooseMedia: ChooseMedia = ChooseMedia(
            "", name = "",
            type = MediaType.IMAGE
        )
    )

    fun showImageUploadingProgress(
        progress: Float, chooseMedia: ChooseMedia =
            ChooseMedia("", name = "", type = MediaType.IMAGE)
    )

    fun showImageUploadingError(
        chooseMedia: ChooseMedia =
            ChooseMedia("", name = "", type = MediaType.IMAGE)
    )
}
