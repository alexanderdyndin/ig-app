package com.intergroupapplication.data.network.dto

import com.google.gson.annotations.SerializedName
import com.intergroupapplication.data.model.ImageVideoModel

data class ImagesDto(
        val count: Int,
        val next: String?,
        val previous: String?,
        @SerializedName("results") val images: List<ImageVideoModel>)