package com.intergroupapplication.data.model.group_followers


import com.google.gson.annotations.SerializedName

data class ResultBan(
        @SerializedName("ban_reason")
        val banReason: String,
        @SerializedName("ban_time")
        val banTime: String,
        @SerializedName("banned_by")
        val bannedBy: BannedBy,
        val group: Int,
        val id: Int,
        val user: User
)