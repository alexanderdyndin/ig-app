package com.intergroupapplication.data.model.group_bans


import com.google.gson.annotations.SerializedName

data class Result(
    @SerializedName("ban_reason")
    val banReason: String,
    @SerializedName("ban_time")
    val banTime: String,
    @SerializedName("banned_by")
    val bannedBy: Int,
    val group: Int,
    val id: Int,
    val user: User
)