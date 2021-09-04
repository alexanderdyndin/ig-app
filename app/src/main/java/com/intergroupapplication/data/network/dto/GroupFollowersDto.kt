package com.intergroupapplication.data.network.dto

import com.intergroupapplication.data.model.GroupFollowerModel

data class GroupFollowersDto(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<GroupFollowerModel>
)