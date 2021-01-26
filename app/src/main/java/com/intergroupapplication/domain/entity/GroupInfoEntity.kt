package com.intergroupapplication.domain.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GroupInfoEntity(val groupId: String, val followersCount: String,
                           val isFollowed: Boolean = false) : Parcelable