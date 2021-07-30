package com.intergroupapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.time.Duration

@Entity
data class AudioModel(
        @PrimaryKey val id: Int,
        val file: String,
        @SerializedName("is_active") val isActive: Boolean,
        val description: String,
        val song: String,
        val artist: String,
        val genre: String,
        val post: Int,
        val owner: Int,
        val duration: String?
)