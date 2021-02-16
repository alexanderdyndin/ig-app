package com.intergroupapplication.domain.entity

import android.os.Parcelable
import com.intergroupapplication.data.model.AuthorModel
import kotlinx.android.parcel.Parcelize

/**
 * Created by abakarmagomedov on 27/08/2018 at project InterGroupApplication.
 */
@Parcelize
data class GroupPostEntity(
        val id: String,
        val groupInPost: GroupInPostEntity,
        val postText: String,
        val date: String,
        val updated: String?,
        val author: AuthorEntity,
        val unreadComments: Int,
        val pin: String?,
        val photo: String?,
        var commentsCount: String,
        val activeCommentsCount: String,
        val isActive: Boolean,
        val isOffered: Boolean,
        val images: List<FileEntity>,
        val audios: List<AudioEntity>,
        val videos: List<FileEntity>
        ) : Parcelable