package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

data class AudioModel(
    val id: Int,
    val file: String,
    @SerializedName("is_active")
    val isActive: Boolean,
    val description: String,
    val song: String,
    val artist: String,
    val genre: String,
    val post: Int,
    val owner: Int,
    val duration: String?
)
