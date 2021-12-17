package com.intergroupapplication.data.network.dto

import com.google.gson.annotations.SerializedName

data class DeviceDto(
    @SerializedName("device_id")
    val deviceId: String?
)
