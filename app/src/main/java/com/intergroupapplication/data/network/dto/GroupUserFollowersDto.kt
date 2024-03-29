package com.intergroupapplication.data.network.dto

import com.intergroupapplication.data.model.groupfollowers.Result


data class GroupUserFollowersDto(
    val count: Int,
    val next: String?,
    val previous: String?,
    var results: List<Result>
)
