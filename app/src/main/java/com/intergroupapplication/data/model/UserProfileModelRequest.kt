package com.intergroupapplication.data.model

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

/**
 * Created by abakarmagomedov on 06/08/2018 at project InterGroupApplication.
 */
class UserProfileModelRequest(
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("second_name")
    val surName: String,
    val birthday: String?,
    val gender: String?,
    @ColumnInfo(name = "user_avatar")
    val avatar: String?
)
