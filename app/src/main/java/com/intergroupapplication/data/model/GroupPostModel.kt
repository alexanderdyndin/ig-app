package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

/**
 * Created by abakarmagomedov on 29/08/2018 at project InterGroupApplication.
 */
data class GroupPostModel(
        val id: String,
        @SerializedName("group") val groupInPost: GroupInPostModel,
        @SerializedName("text") val postText: String,
        val date: String,
        val updated: String?,
        val author: AuthorModel,
        @SerializedName("unread_comments_count") val unreadComments: Int,
        @SerializedName("pin_time") val pin: String?,
        @SerializedName("file") val photo: String?,
        @SerializedName("comments_count") val commentsCount: String,
        @SerializedName("active_comments_count") val activeCommentsCount: String,
        @SerializedName("is_active") val isActive: Boolean,
        @SerializedName("is_offered") val isOffered: Boolean,
        @SerializedName("is_liked")
        val isLiked: Boolean,
        @SerializedName("is_disliked")
        val isDisliked: Boolean,
        @SerializedName("likes_count")
        val likesCount: Int,
        @SerializedName("dislikes_count")
        val dislikesCount: Int,
        val images: List<ImageVideoModel>,
        val audios: List<AudioModel>,
        val videos: List<ImageVideoModel>
        )