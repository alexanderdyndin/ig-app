package com.intergroupapplication.data.db.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.intergroupapplication.data.model.*

@Entity(tableName = "News")
data class NewsPostDb(
    @PrimaryKey
    val id: Int,
    val postId: String,
    val groupId: String,
    val activeCommentsCount: String,
    @Embedded
    val groupInPost: GroupInPostModel,
    @Embedded
    val bells: BellsModel,
    @Embedded
    val reacts: ReactsModel,
    val images: List<ImageVideoModel>,
    val audios: List<AudioModel>,
    val videos: List<ImageVideoModel>,
    val photo: String?,
    val postText: String,
    val date: String,
    val updated: String?,
    val commentsCount: String,
    val isActive: Boolean,
    val isOffered: Boolean,
    val idp: Int,
    val isPinned: Boolean,
    val pin: String?,
    @Embedded
    val author: AuthorModel,
    val unreadComments: String,
    val user: Int
)
