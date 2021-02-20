package com.intergroupapplication.domain.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AudioEntity (
        val id: Int,
        val file: String,               //todo что за названия переменных блеать? Какой файл, ккакой song?
        val isActive: Boolean,
        val description: String,
        val song: String,
        val artist: String,
        val genre: String,
        val post: Int,
        val owner: Int
        ): Parcelable