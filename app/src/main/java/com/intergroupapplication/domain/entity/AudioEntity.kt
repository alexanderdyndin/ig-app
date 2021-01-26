package com.intergroupapplication.domain.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AudioEntity (
        val id: Int,
        val file: String,
        val description: String,
        val song: String,
        val artist: String,
        val genre: String,
        val post: Int,
        val owner: Int
        ): Parcelable