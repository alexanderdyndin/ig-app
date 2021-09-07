package com.intergroupapplication.data.network.dto

import com.google.gson.annotations.SerializedName

data class AuthorDto(
    val id: Int,
    val email: String,
    @SerializedName("is_blocked")
    val isBlocked: Boolean,
    @SerializedName("is_verified")
    val isVerified: Boolean,
    @SerializedName("time_blocked")
    val timeBlocked: String?,
    val profile: UserProfileDto
)
