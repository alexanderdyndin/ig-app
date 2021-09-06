package com.intergroupapplication.data.network.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    val id: String,
    val email: String,
    @SerializedName("isBlocked")
    val isBlocked: Boolean,
    @SerializedName("isActive")
    val isActive: Boolean
)
