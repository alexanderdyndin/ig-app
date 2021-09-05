package com.intergroupapplication.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BellsEntity(
    var count: Int,
    var isActive: Boolean
) : Parcelable
