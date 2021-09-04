package com.intergroupapplication.data.network.dto

import com.google.gson.annotations.SerializedName

data class GroupDto(
    val id: String,
    val avatar: String?,
    @SerializedName("followers_count")
    val followersCount: String,
    @SerializedName("is_followed")
    val isFollowing: Boolean,
    @SerializedName("posts_count")
    val postsCount: String,
    @SerializedName("posts_likes")
    val postsLikes: String,
    @SerializedName("posts_dislikes")
    val postsDislikes: String,
    @SerializedName("comments_count")
    val commentsCount: String,
    val name: String,
    val description: String,
    val rules: String,
    @SerializedName("is_blocked")
    val isBlocked: Boolean,
    @SerializedName("time_blocked")
    val timeBlocked: String?,
    @SerializedName("is_closed")
    val isClosed: Boolean,
    @SerializedName("age_restriction")
    val ageRestriction: String,
    val subject: String,
    val owner: String
)
