package com.intergroupapplication.domain.entity

data class GroupFollowEntity(
    val id: Int,
    val is_blocked: Boolean,
    val time_blocked: String,
    val user: String,
    val group: Int
)
