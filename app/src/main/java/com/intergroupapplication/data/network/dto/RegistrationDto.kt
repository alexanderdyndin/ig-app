package com.intergroupapplication.data.network.dto

import com.google.gson.annotations.SerializedName

data class RegistrationDto(
    val email: String,
    val password: String,
    @SerializedName("email_confirm")
    val emailConfirm: String,
    @SerializedName("password_confirm")
    val passwordConfirm: String
)
