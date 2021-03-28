package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

/**
 * Created by abakarmagomedov on 29/08/2018 at project InterGroupApplication.
 */
data class GroupPostModel(
        val id: String,
        @SerializedName("active_comments_count") val activeCommentsCount: String,
        @SerializedName("group") val groupInPost: GroupInPostModel,
        val bells: BellsModel,
        val reacts: ReactsModel,
        val images: List<ImageVideoModel>,
        val audios: List<AudioModel>,
        val videos: List<ImageVideoModel>,
        @SerializedName("file") val photo: String?,
        @SerializedName("text") val postText: String,
        val date: String,
        val updated: String?,
        @SerializedName("comments_count") val commentsCount: String,
        @SerializedName("is_active") val isActive: Boolean,
        @SerializedName("is_offered") val isOffered: Boolean,
        @SerializedName("unique_index") val idp: Int,
        @SerializedName("is_pinned") val isPinned: Boolean,
        @SerializedName("pin_time") val pin: String?,
        val author: AuthorModel
        )