package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

data class AudiosDto(
        val count: Int,
        val next: String?,
        val previous: String?,
        @SerializedName("results") val audios: List<AudioModel>)