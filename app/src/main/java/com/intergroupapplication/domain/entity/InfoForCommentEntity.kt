package com.intergroupapplication.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by abakarmagomedov on 03/09/2018 at project InterGroupApplication.
 */
@Parcelize
data class InfoForCommentEntity(
    val groupPostEntity: GroupPostEntity.PostEntity,
    val isFromNewsScreen: Boolean = false
) : Parcelable
