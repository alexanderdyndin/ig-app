package com.intergroupapplication.data.model.group_followers


data class GroupUserFollowersDto(
    val count: Int,
    val next: String?,
    val previous: String?,
    var results: List<Result>
)