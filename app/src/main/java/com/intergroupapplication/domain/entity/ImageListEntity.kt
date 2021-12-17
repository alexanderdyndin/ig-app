package com.intergroupapplication.domain.entity

data class ImageListEntity(
    val count: Int,
    val next: String?,
    val previous: String?,
    val images: List<FileEntity>
)
