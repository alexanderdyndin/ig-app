package com.intergroupapplication.data.network.dto

import com.google.gson.annotations.SerializedName

data class NewPasswordDto(
    val token: String,
    val password: String,
    @SerializedName("password_confirm")
    val passwordConfirm: String
)
