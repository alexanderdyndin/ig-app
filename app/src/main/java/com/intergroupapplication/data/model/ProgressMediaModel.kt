package com.intergroupapplication.data.model

import android.os.Parcelable
import com.intergroupapplication.domain.entity.LoadMediaType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ProgressMediaModel(val url:String, val type:LoadMediaType):Parcelable