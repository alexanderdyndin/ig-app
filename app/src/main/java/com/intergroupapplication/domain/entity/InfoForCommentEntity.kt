package com.intergroupapplication.domain.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by abakarmagomedov on 03/09/2018 at project InterGroupApplication.
 */
@Parcelize
data class InfoForCommentEntity(val groupPostEntity: GroupPostEntity,
                                val isFromNewsScreen: Boolean = false) : Parcelable