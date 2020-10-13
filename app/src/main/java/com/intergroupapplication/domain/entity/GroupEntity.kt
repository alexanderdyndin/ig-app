package com.intergroupapplication.domain.entity

import com.google.gson.annotations.SerializedName

/**
 * Created by abakarmagomedov on 02/08/2018 at project InterGroupApplication.
 */
data class GroupEntity(val id: String,
                       val followersCount: String,
                       val postsCount: String,
                       val postsLikes: String,
                       val postsDislikes: String,
                       val CommentsCount: String,
                       val timeBlocked: String?,
                       val name: String,
                       val description: String,
                       val isBlocked: Boolean,
                       val owner: String,
                       val isFollowing: Boolean,
                       val avatar: String?)
