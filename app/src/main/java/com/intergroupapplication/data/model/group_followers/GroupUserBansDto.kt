package com.intergroupapplication.data.model.group_followers


data class GroupUserBansDto(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<ResultBan>
)