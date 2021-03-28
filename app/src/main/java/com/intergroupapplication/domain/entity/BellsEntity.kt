package com.intergroupapplication.domain.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BellsEntity(
        val count: Int,
        val isActive: Boolean
): Parcelable
