package com.intergroupapplication.domain.entity

data class GroupUserEntity(
        val firstName: String,
        val surName: String,
        val avatar: String,
        val idProfile: String,
        val commentsCount: Int,
        val dislikeCount: Int,
        val likesCount: Int,
        val postsCount: Int,
        var isAdministrator: Boolean,
        val isOwner: Boolean,
        var isBlocked: Boolean,
        val banId: String = "",
        val subscriptionId: String = ""
)