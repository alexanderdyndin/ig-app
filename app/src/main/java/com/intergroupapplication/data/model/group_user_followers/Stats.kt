package com.intergroupapplication.data.model.group_user_followers


import com.google.gson.annotations.SerializedName

data class Stats(
    @SerializedName("comments")
    val comments: Int,
    @SerializedName("dislikes")
    val dislikes: Int,
    @SerializedName("likes")
    val likes: Int,
    @SerializedName("posts")
    val posts: Int
)