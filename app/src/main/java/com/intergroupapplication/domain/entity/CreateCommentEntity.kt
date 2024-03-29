package com.intergroupapplication.domain.entity

/**
 * Created by abakarmagomedov on 03/09/2018 at project InterGroupApplication.
 */
data class CreateCommentEntity(
    val text: String,
    val images: List<FileRequestEntity>,
    val audios: List<AudioRequestEntity>,
    val videos: List<FileRequestEntity>,
)
