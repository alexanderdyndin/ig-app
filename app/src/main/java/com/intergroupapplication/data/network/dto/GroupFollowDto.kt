package com.intergroupapplication.data.network.dto

data class GroupFollowDto(
    val id: Int,
    val is_blocked: Boolean,
    val time_blocked: String,
    val user: String,
    val group: Int
)
