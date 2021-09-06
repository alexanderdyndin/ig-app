package com.intergroupapplication.data.network.dto

import com.google.gson.annotations.SerializedName
import com.intergroupapplication.data.model.AudioRequestModel
import com.intergroupapplication.data.model.FileRequestModel

/**
 * Created by abakarmagomedov on 29/08/2018 at project InterGroupApplication.
 */
data class CreateGroupPostDto(
    @SerializedName("text")
    val postText: String,
    val images: List<FileRequestModel>,
    val audios: List<AudioRequestModel>,
    val videos: List<FileRequestModel>,
    @SerializedName("is_pinned")
    val isPinned: Boolean,
    @SerializedName("pin_time")
    val pinTime: String?
)
