package com.intergroupapplication.data.db.entity

import androidx.room.ColumnInfo

data class GroupDb(
    @ColumnInfo(name = "group_id")
    val id: String,
    val avatar: String?,
    @ColumnInfo(name = "followers_count")
    val followersCount: String,
    @ColumnInfo(name = "is_followed")
    val isFollowing: Boolean,
    @ColumnInfo(name = "posts_count")
    val postsCount: String,
    @ColumnInfo(name = "posts_likes")
    val postsLikes: String,
    @ColumnInfo(name = "posts_dislikes")
    val postsDislikes: String,
    @ColumnInfo(name = "comments_count")
    val commentsCount: String,
    val name: String,
    val description: String,
    val rules: String,
    @ColumnInfo(name = "is_blocked_group")
    val isBlocked: Boolean,
    @ColumnInfo(name = "time_blocked_group")
    val timeBlocked: String?,
    @ColumnInfo(name = "is_closed_group")
    val isClosed: Boolean,
    @ColumnInfo(name = "age_restriction")
    val ageRestriction: String,
    val subject: String,
    val owner: String
)
