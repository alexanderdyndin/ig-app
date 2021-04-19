package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

data class ImagesDto(
        val count: Int,
        val next: String?,
        val previous: String?,
        @SerializedName("results") val images: List<ImageVideoModel>)