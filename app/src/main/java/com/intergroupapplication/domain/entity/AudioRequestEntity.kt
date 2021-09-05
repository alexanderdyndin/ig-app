package com.intergroupapplication.domain.entity

data class AudioRequestEntity(
    val urlFile: String,
    val description: String?,
    val nameSong: String?,
    val artist: String?,
    val genre: String?,
    val duration: String?
)
