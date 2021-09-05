package com.intergroupapplication.data.model

import android.os.Parcelable
import com.intergroupapplication.domain.entity.LoadMediaType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ProgressMediaModel(
    val chooseMedia: ChooseMedia,
    val type: LoadMediaType
) : Parcelable