package com.intergroupapplication.data.network.dto

import com.google.gson.annotations.SerializedName
import com.intergroupapplication.data.model.AudioModel

data class AudiosDto(
    val count: Int,
    val next: String?,
    val previous: String?,
    @SerializedName("results")
    val audios: List<AudioModel>
)
