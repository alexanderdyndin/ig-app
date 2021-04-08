package com.intergroupapplication.data.model.group_bans


import com.google.gson.annotations.SerializedName

data class User(
    val email: String,
    val id: Int,
    @SerializedName("is_blocked")
    val isBlocked: Boolean,
    @SerializedName("is_verified")
    val isVerified: Boolean,
    val profile: Profile,
    @SerializedName("time_blocked")
    val timeBlocked: String
)