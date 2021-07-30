package com.intergroupapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class ImageVideoModel(
        @PrimaryKey val id: Int,
        val file: String,
        @SerializedName("is_active") val isActive: Boolean,
        val description: String,
        val title: String,
        val post: Int,
        val owner: Int,
        val preview:String?,
        val duration:String?
)