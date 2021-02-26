package com.intergroupapplication.domain.entity

import com.intergroupapplication.data.model.GroupPostModel

data class GroupPostsEntity(
        val count: Int,
        val next: String?,
        val previous: String?,
        val results: List<GroupPostEntity>
)
