package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

data class CommentUserModel(
        val user: Int,
        @SerializedName("first_name") val firstName: String,
        @SerializedName("second_name") val secondName: String,
        val birthday: String,
        val gender: String,
        val avatar: String?)