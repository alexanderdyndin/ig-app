package com.intergroupapplication.domain.entity

data class GroupFollowersListEntity (
        val count: Int,
        val next: String?,
        val previous: String?,
        val users: List<GroupUserEntity>)