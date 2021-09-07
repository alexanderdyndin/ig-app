package com.intergroupapplication.data.model

import com.intergroupapplication.domain.entity.AudioRequestEntity
import com.intergroupapplication.domain.entity.FileRequestEntity

/**
 * Created by abakarmagomedov on 03/09/2018 at project InterGroupApplication.
 */
data class CreateCommentModel(
    val text: String,
    val images: List<FileRequestEntity>,
    val audios: List<AudioRequestEntity>,
    val videos: List<FileRequestEntity>,
)
