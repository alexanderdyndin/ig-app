package com.intergroupapplication.data.db.entity

import androidx.room.ColumnInfo

data class UserProfileDb(
    val firstName: String,
    val surName: String,
    val birthday: String?,
    val gender: String?,
    @ColumnInfo(name = "user_avatar")
    val avatar: String?
)
