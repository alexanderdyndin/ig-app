package com.intergroupapplication.data.network.dto

import com.intergroupapplication.data.model.group_followers.ResultBan


data class GroupUserBansDto(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<ResultBan>
)