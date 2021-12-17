package com.intergroupapplication.data.db.entity

import androidx.room.ColumnInfo

data class BellsDb(
    @ColumnInfo(name = "bells_count")
    val count: Int,
    @ColumnInfo(name = "bell_is_active")
    val isActive: Boolean
)
