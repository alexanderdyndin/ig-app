package com.intergroupapplication.data.network.dto

import com.google.gson.annotations.SerializedName
import com.intergroupapplication.data.model.*

/**
 * Created by abakarmagomedov on 29/08/2018 at project InterGroupApplication.
 */
data class GroupPostDto(
    val id: String,
    var groupId: String = "",
    @SerializedName("active_comments_count")
    val activeCommentsCount: String,
    @SerializedName("group")
    val groupInPost: GroupDto,
    val bells: BellsModel,
    val reacts: ReactsModel,
    val images: List<ImageVideoModel>,
    val audios: List<AudioModel>,
    val videos: List<ImageVideoModel>,
    @SerializedName("file")
    val photo: String?,
    @SerializedName("text")
    val postText: String,
    val date: String,
    val updated: String?,
    @SerializedName("comments_count")
    val commentsCount: String,
    @SerializedName("is_active")
    val isActive: Boolean,
    @SerializedName("is_offered")
    val isOffered: Boolean,
    @SerializedName("unique_index")
    val idp: Int,
    @SerializedName("is_pinned")
    val isPinned: Boolean,
    @SerializedName("pin_time")
    val pin: String?,
    val author: AuthorModel,
    @SerializedName("unread_comments_count")
    val unreadComments: String
)
