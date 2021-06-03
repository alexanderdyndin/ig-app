package com.intergroupapplication.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "group_post_remote_keys")
data class GroupPostRemoteKeysModel(
        @PrimaryKey val groupId: String,
        val next: Int?,
        val previous: Int?
)
