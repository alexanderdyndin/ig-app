package com.intergroupapplication.domain.entity

import com.intergroupapplication.data.model.GroupModel

data class VideoListEntity(
        val count: Int,
        val next: String?,
        val previous: String?,
        val videos: List<FileEntity>)