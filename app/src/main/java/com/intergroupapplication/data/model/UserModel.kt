package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

data class UserModel(
        val id: String,
        val email: String,
        @SerializedName("isBlocked")
        val isBlocked: Boolean,
        @SerializedName("isActive")
        val isActive: Boolean
)