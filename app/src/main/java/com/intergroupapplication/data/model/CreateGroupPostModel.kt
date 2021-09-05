package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

/**
 * Created by abakarmagomedov on 29/08/2018 at project InterGroupApplication.
 */
data class CreateGroupPostModel(
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
