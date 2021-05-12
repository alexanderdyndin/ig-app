package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

data class ImageVideoModel(
        val id: Int,
        val file: String,
        @SerializedName("is_active") val isActive: Boolean,
        val description: String,
        val title: String,
        val post: Int,
        val owner: Int,
        val preview:String?
)