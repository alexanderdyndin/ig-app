package com.intergroupapplication.domain.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AuthorEntity(
        val id: Int,
        val email: String,
        val isBlocked: Boolean,
        val isVerified: Boolean,
        val timeBlocked: String?,
        val profile: UserProfileEntityRequest): Parcelable