package com.intergroupapplication.data.model

data class ImageVideoModel(
        val id: Int,
        val file: String,
        val description: String,
        val title: String,
        val post: Int,
        val owner: Int
)