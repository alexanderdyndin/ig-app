package com.intergroupapplication.data.network.dto

import com.intergroupapplication.data.model.PhotoUploadFields

data class ImageUploadDto(
    val url: String,
    val fields: PhotoUploadFields,
)