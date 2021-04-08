package com.intergroupapplication.data.model.group_bans


data class GroupUserBansDto(
    val count: Int,
    val next: String,
    val previous: String,
    val results: List<Result>
)