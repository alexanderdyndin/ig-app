package com.intergroupapplication.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.intergroupapplication.data.model.AudioModel
import com.intergroupapplication.data.model.ImageVideoModel

@Entity(tableName = "group_post")
data class GroupPostDb(
    @PrimaryKey
    @ColumnInfo(name = "post_id")
    val id: String,
    val activeCommentsCount: String,
    @Embedded
    val groupInPost: GroupDb,
    @Embedded
    val bells: BellsDb,
    @Embedded
    val reacts: ReactsDb,
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
    val author: AuthorDb,
    val unreadComments: String
)
