package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

data class RegistrationModel(
    val email: String,
    val password: String,
    @SerializedName("email_confirm")
    val emailConfirm: String,
    @SerializedName("password_confirm")
    val passwordConfirm: String
)
