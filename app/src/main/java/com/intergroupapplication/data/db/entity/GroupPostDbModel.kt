package com.intergroupapplication.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.intergroupapplication.data.model.*

@Entity(tableName = "group_post")
data class GroupPostDbModel(
        @PrimaryKey val id: String,
         val groupId: String, //@PrimaryKey
        val bells: BellsModel,
        val groupInPost: GroupInPostModel,
        val postText: String,
        val date: String,
        val updated: String?,
        val author: AuthorModel,
        val pin: String?,
        val photo: String?,
        var commentsCount: String,
        val activeCommentsCount: String,
        val isActive: Boolean,
        val isOffered: Boolean,
        var isPinned: Boolean,
        var reacts: ReactsModel,
        val idp: Int,
        val images: List<ImageVideoModel>,
        val audios: List<AudioModel>,
        val videos: List<ImageVideoModel>,
        var isLoading: Boolean = false,
        val unreadComments: String,
        var imagesExpanded: Boolean = false,
        var audiosExpanded: Boolean = false,
        var videosExpanded: Boolean = false
)