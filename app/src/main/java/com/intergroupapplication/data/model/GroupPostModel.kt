package com.intergroupapplication.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Created by abakarmagomedov on 29/08/2018 at project InterGroupApplication.
 */
@Entity(tableName = "group_post")
data class GroupPostModel(
    @PrimaryKey
    val id: String,
    var groupId: String = "",
    @SerializedName("active_comments_count")
    val activeCommentsCount: String,
    @Embedded
    @SerializedName("group")
    val groupInPost: GroupInPostModel,
    @Embedded val bells: BellsModel,
    @Embedded val reacts: ReactsModel,
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
    @Embedded
    val author: AuthorModel,
    @SerializedName("unread_comments_count")
    val unreadComments: String
)