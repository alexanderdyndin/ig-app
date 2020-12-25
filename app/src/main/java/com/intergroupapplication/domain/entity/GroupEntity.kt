package com.intergroupapplication.domain.entity

import com.google.gson.annotations.SerializedName

/**
 * Created by abakarmagomedov on 02/08/2018 at project InterGroupApplication.
 */
data class GroupEntity(val id: String,
                       var followersCount: String,
                       var postsCount: String,
                       var postsLikes: String,
                       var postsDislikes: String,
                       var CommentsCount: String,
                       val timeBlocked: String?,
                       val name: String,
                       val description: String,
                       val isBlocked: Boolean,
                       val owner: String,
                       var isFollowing: Boolean,
                       val avatar: String?,
                       val subject: String,
                       val rules: String,
                       val isClosed: Boolean,
                       val ageRestriction: String)
