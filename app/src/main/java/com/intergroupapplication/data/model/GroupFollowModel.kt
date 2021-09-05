package com.intergroupapplication.data.model

data class GroupFollowModel(
    val id: Int,
    val is_blocked: Boolean,
    val time_blocked: String,
    val user: String,
    val group: Int
)
