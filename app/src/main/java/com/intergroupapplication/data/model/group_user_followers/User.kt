package com.intergroupapplication.data.model.group_user_followers


import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("email")
    val email: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("is_blocked")
    val isBlocked: Boolean,
    @SerializedName("is_verified")
    val isVerified: Boolean,
    @SerializedName("profile")
    val profile: Profile,
    @SerializedName("time_blocked")
    val timeBlocked: String?
)