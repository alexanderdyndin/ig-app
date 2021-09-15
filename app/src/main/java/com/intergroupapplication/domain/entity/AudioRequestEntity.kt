package com.intergroupapplication.domain.entity

import com.google.gson.annotations.SerializedName

data class AudioRequestEntity(
    @SerializedName("file")
    val urlFile: String,
    val description: String?,
    @SerializedName("song")
    val nameSong: String?,
    val artist: String?,
    val genre: String?,
    val duration: String?
)
