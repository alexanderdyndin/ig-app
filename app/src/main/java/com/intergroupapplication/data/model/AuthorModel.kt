package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

data class AuthorModel(val email: String,
                       @SerializedName("is_blocked") val isBlocked: Boolean,
                       @SerializedName("is_verified") val isVerified: Boolean,
                       @SerializedName("time_blocked") val timeBlocked: String?,
                       val profile: UserProfileModelRequest
                       )