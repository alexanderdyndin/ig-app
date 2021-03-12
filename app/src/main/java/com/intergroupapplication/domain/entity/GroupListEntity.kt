package com.intergroupapplication.domain.entity

import com.intergroupapplication.data.model.GroupModel

data class GroupListEntity(
        val count: Int,
        val next: String?,
        val previous: String?,
        val groups: List<GroupEntity>)