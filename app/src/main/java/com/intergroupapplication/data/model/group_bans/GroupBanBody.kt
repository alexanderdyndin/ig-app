package com.intergroupapplication.data.model.group_bans

import com.google.gson.annotations.SerializedName

data class GroupBanBody(
        @SerializedName("ban_reason") val banReason: String,
        val user: String
)
