package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

/**
 * Created by abakarmagomedov on 29/08/2018 at project InterGroupApplication.
 */
data class GroupPostModel(
        val id: String,
        @SerializedName("text") val postText: String,
        @SerializedName("comments_count") val commentsCount: String?,
        @SerializedName("date") val date: String,
        @SerializedName("group") val groupInPost: GroupInPostModel,
        @SerializedName("file") val photo: String?)