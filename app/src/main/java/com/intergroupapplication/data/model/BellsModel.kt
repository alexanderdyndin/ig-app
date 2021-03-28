package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

data class BellsModel(
        @SerializedName("bells_count")
        val count: Int,
        @SerializedName("bell_is_active")
        val isActive: Boolean
)
