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
        @SerializedName("file") val photo: String?,
        @SerializedName("comments_count") val commentsCount: String?,
        @SerializedName("active_comments_count") val activeCommentsCount: String? = null,
        @SerializedName("is_active") val isActive: Boolean? = null,
        @SerializedName("is_offered") val isOffered: Boolean? = null,
        @SerializedName("image_files") val images: List<ImageVideoModel>? = null,
        @SerializedName("audio_files") val audios: List<AudioModel>? = null,
        @SerializedName("video_files") val videos: List<ImageVideoModel>? = null
        )