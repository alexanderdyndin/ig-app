package com.intergroupapplication.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "news_post_remote_key")
data class NewsPostRemoteKeyDb(
    @PrimaryKey
    val id: Int = 0,
    val prevKey: Int?,
    val nextKey: Int?
)
