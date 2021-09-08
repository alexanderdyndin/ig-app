package com.intergroupapplication.data.network.dto

import com.google.gson.annotations.SerializedName

data class BellsDto(
    @SerializedName("bells_count")
    val count: Int,
    @SerializedName("bell_is_active")
    val isActive: Boolean
)
