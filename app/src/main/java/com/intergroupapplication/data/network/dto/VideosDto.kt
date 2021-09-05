package com.intergroupapplication.data.network.dto

import com.google.gson.annotations.SerializedName
import com.intergroupapplication.data.model.ImageVideoModel

data class VideosDto(
    val count: Int,
    val next: String?,
    val previous: String?,
    @SerializedName("results")
    val videos: List<ImageVideoModel>
)