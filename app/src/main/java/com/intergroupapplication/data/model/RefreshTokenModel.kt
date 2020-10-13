package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

/**
 * Created by abakarmagomedov on 21/09/2018 at project InterGroupApplication.
 */
data class RefreshTokenModel(@SerializedName("refresh") val refreshToken: String)