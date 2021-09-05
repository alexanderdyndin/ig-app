package com.intergroupapplication.domain.entity

data class GroupListEntity(
    val count: Int,
    val next: String?,
    val previous: String?,
    val groups: List<GroupEntity.Group>
)
