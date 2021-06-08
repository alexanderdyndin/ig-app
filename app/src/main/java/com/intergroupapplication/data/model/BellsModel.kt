package com.intergroupapplication.data.model

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

data class BellsModel(
        @ColumnInfo(name = "bells_count")
        @SerializedName("bells_count")
        val count: Int,
        @ColumnInfo(name = "bell_is_active")
        @SerializedName("bell_is_active")
        val isActive: Boolean
)
