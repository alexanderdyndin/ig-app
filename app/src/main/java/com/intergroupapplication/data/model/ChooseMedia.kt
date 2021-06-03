package com.intergroupapplication.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChooseMedia(val url:String,
                       val urlPreview:String ="",
                       val name:String = "",
                       val authorMusic:String = "",
                       val duration:String = ""):Parcelable