package com.intergroupapplication.data.model

data class AudioModel(
        val id: Int,
        val file: String,
        val description: String,
        val song: String,
        val artist: String,
        val genre: String,
        val post: Int,
        val owner: Int
)