package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

data class VideosDto(
        val count: Int,
        val next: String?,
        val previous: String?,
        @SerializedName("results") val videos: List<ImageVideoModel>)