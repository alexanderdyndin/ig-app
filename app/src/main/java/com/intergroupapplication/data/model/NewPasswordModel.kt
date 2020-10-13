package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

data class NewPasswordModel(val token: String,
                            val password: String,
                            @SerializedName("password_confirm")
                            val passwordConfirm: String)