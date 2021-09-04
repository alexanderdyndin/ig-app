package com.intergroupapplication.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.google.gson.annotations.SerializedName

data class AuthorModel(
        @ColumnInfo(name = "author_id") val id: Int,
        @ColumnInfo(name = "author_mail") val email: String,
        @SerializedName("is_blocked") val isBlocked: Boolean,
        @SerializedName("is_verified") val isVerified: Boolean,
        @SerializedName("time_blocked") val timeBlocked: String?,
        @Embedded val profile: UserProfileModelRequest)