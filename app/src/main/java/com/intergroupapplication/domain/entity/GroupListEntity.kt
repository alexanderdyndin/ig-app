package com.intergroupapplication.domain.entity

import com.intergroupapplication.data.model.GroupModel

data class GroupListEntity(
        val count: Int,
        val next: Int?,
        val previous: Int?,
        val groups: List<GroupEntity>)