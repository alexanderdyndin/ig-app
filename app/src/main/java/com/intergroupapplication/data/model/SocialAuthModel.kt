package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

data class SocialAuthModel(
    @SerializedName("auth_token")
    val authToken: String
)
