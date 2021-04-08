package com.intergroupapplication.domain.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BellsEntity(
        var count: Int,
        var isActive: Boolean
): Parcelable
