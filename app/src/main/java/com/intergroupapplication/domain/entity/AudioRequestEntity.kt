package com.intergroupapplication.domain.entity

data class AudioRequestEntity (
        val file: String,
        val description: String?,
        val song: String?,
        val artist: String?,
        val genre: String?
)