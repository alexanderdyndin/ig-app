package com.intergroupapplication.domain.entity

import com.intergroupapplication.data.model.GroupModel

data class ImageListEntity(
        val count: Int,
        val next: String?,
        val previous: String?,
        val images: List<FileEntity>)