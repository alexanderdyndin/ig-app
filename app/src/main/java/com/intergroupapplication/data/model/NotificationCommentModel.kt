package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

/**
 * Created by abakarmagomedov on 05/09/2018 at project InterGroupApplication.
 */
data class NotificationCommentModel(
    @SerializedName("post_id") val postId: String,
    @SerializedName("name") val name: String,
    @SerializedName("group_id") val groupId: String,
    @SerializedName("comment_id") val commentId: String,
    @SerializedName("message") val message: String,
    val page: String = "1"
)
