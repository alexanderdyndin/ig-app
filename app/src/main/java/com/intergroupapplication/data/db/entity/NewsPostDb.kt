package com.intergroupapplication.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "News")
data class NewsPostDb(
    @PrimaryKey
    @ColumnInfo(name = "news_id")
    val id: Int,
    @Embedded
    val post: GroupPostDb,
    val user: Int
)
