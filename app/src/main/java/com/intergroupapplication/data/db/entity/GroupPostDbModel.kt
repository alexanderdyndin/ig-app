package com.intergroupapplication.data.db.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.intergroupapplication.data.model.*

@Entity(tableName = "group_post")
data class GroupPostDbModel(
        @PrimaryKey val id: String,
        val groupId: String,
        @Embedded val bells: BellsModel,
        @Embedded val groupInPost: GroupInPostModel,
        val postText: String,
        val date: String,
        val updated: String?,
        @Embedded val author: AuthorModel,
        val pin: String?,
        val photo: String?,
        var commentsCount: String,
        val activeCommentsCount: String,
        val isActive: Boolean,
        val isOffered: Boolean,
        var isPinned: Boolean,
        @Embedded var reacts: ReactsModel,
        val idp: Int,
        var isLoading: Boolean = false,
        val unreadComments: String,
        var imagesExpanded: Boolean = false,
        var audiosExpanded: Boolean = false,
        var videosExpanded: Boolean = false,
)