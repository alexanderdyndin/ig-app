package com.intergroupapplication.domain.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FileEntity(
        val id: Int,
        val file: String,
        val isActive: Boolean,
        val description: String,
        val title: String,
        val post: Int,
        val owner: Int,
        val preview:String = "",
        val duration:String = ""
): Parcelable