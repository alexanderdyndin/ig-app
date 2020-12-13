package com.intergroupapplication.domain.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by abakarmagomedov on 27/08/2018 at project InterGroupApplication.
 */
@Parcelize
data class GroupPostEntity(
        val id: String,
        val postText: String,
        var commentsCount: String,
        val date: String,
        val groupInPost: GroupInPostEntity,
        val photo: String?
        ) : Parcelable