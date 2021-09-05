package com.intergroupapplication.domain.entity

data class VideoListEntity(
        val count: Int,
        val next: String?,
        val previous: String?,
        val videos: List<FileEntity>)