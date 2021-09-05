package com.intergroupapplication.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReactsEntity(
    val isLike: Boolean,
    val isDislike: Boolean,
    val likesCount: Int,
    val dislikesCount: Int
) : Parcelable
