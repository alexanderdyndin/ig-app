package com.intergroupapplication.data.db.entity

import androidx.room.ColumnInfo

data class ReactsDb(
    @ColumnInfo(name = "is_like")
    val isLike: Boolean,
    @ColumnInfo(name = "is_dislike")
    val isDislike: Boolean,
    @ColumnInfo(name = "likes_count")
    val likesCount: Int,
    @ColumnInfo(name = "dislikes_count")
    val dislikesCount: Int
)
