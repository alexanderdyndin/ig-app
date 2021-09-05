package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

data class ReactsModel(
    @SerializedName("is_like")
    val isLike: Boolean,
    @SerializedName("is_dislike")
    val isDislike: Boolean,
    @SerializedName("likes_count")
    val likesCount: Int,
    @SerializedName("dislikes_count")
    val dislikesCount: Int
)
