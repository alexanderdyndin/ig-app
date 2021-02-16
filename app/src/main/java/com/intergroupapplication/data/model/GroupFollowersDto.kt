package com.intergroupapplication.data.model

data class GroupFollowersDto(
        val count: Int,
        val next: String?,
        val previous: String?,
        val results: List<GroupFollowerModel>
)