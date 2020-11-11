package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

data class GroupModel(
        val id: String,
        @SerializedName("followers_count") val followersCount: String,
        @SerializedName("posts_count") val postsCount: String,
        @SerializedName("posts_likes") val postsLikes: String,
        @SerializedName("posts_dislikes") val postsDislikes: String,
        @SerializedName("comments_count") val CommentsCount: String,
        @SerializedName("time_blocked") val timeBlocked: String?,
        val name: String,
        val description: String,
        @SerializedName("is_blocked") val isBlocked: Boolean,
        val owner: String,
        @SerializedName("is_followed") val isFollowing: Boolean,
        val avatar: String?,
        val subject: String,
        val rules: String,
        @SerializedName("is_closed") val isClosed: Boolean,
        @SerializedName("age_restriction") val ageRestriction: String
)