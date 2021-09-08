package com.intergroupapplication.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "group_post_remote_keys")
data class GroupPostRemoteKeysDb(
        @PrimaryKey
        @ColumnInfo(name = "group_id")
        val groupId: String,
        val prevKey: Int?,
        val nextKey: Int?
)
