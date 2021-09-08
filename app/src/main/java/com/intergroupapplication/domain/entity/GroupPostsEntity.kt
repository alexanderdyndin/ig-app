package com.intergroupapplication.domain.entity

data class GroupPostsEntity(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<GroupPostEntity.PostEntity>
)
