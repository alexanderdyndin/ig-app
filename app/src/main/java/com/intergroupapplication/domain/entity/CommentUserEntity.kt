package com.intergroupapplication.domain.entity

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by abakarmagomedov on 03/09/2018 at project InterGroupApplication.
 */
@Parcelize
data class CommentUserEntity(
        val user: Int,
        val firstName: String,
        val secondName: String,
        val birthday: String,
        val gender: String,
        val avatar: String?
):Parcelable