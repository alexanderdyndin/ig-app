package com.intergroupapplication.domain.entity

import com.intergroupapplication.data.model.GroupModel

data class AudioListEntity(
        val count: Int,
        val next: String?,
        val previous: String?,
        val audios: List<AudioEntity>)