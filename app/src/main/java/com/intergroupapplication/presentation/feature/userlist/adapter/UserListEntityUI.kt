package com.intergroupapplication.presentation.feature.userlist.adapter

data class UserListEntityUI(
        val firstName: String,
        val surName: String,
        val avatar: String,
        val idProfile: String,
        val commentsCount: Int,
        val dislikesCount: Int,
        val likesCount: Int,
        val postsCount: Int,
        val isAdministrator: Boolean
)
