package com.intergroupapplication.domain.entity

data class FileRequestEntity(
    val file: String,
    val description: String?,
    val title: String?,
    val preview: String = "",
    val duration: String = "00:00"
)
