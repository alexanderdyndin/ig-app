package com.intergroupapplication.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class AuthorDb(
    @ColumnInfo(name = "author_id")
    val id: Int,
    @ColumnInfo(name = "author_mail")
    val email: String,
    @ColumnInfo(name = "is_blocked")
    val isBlocked: Boolean,
    @ColumnInfo(name = "is_verified")
    val isVerified: Boolean,
    @ColumnInfo(name = "time_blocked")
    val timeBlocked: String?,
    @Embedded
    val profile: UserProfileDb
)
