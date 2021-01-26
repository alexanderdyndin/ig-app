package com.intergroupapplication.domain.entity

import android.os.Parcelable
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
        val photo: String?,
        var commentsCount: String?,
        val activeCommentsCount: String? = null,
        val isActive: Boolean? = null,
        val isOffered: Boolean? = null,
        val images: List<FileEntity>? = null,
        val audios: List<AudioEntity>? = null,
        val videos: List<FileEntity>? = null
        ) : Parcelable