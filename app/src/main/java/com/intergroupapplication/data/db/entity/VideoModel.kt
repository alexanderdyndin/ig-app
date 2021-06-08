package com.intergroupapplication.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class VideoModel(
        @PrimaryKey val id: Int,
        val file: String,
        val isActive: Boolean,
        val description: String,
        val title: String,
        val post: Int,
        val owner: Int)
