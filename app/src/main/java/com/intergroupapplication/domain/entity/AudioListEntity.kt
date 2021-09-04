package com.intergroupapplication.domain.entity

data class AudioListEntity(
        val count: Int,
        val next: String?,
        val previous: String?,
        val audios: List<AudioEntity>)