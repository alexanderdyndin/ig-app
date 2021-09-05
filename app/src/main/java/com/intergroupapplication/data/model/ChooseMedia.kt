package com.intergroupapplication.data.model

import android.os.Parcelable
import com.intergroupapplication.domain.entity.MediaType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChooseMedia(
    val url: String,
    val urlPreview: String = "",
    val name: String,
    val author: String = "",
    val duration: String = "",
    val type: MediaType
) : Parcelable
