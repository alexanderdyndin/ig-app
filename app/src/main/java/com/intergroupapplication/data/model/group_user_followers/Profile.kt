package com.intergroupapplication.data.model.group_user_followers


import com.google.gson.annotations.SerializedName

data class Profile(
    @SerializedName("avatar")
    val avatar: String,
    @SerializedName("birthday")
    val birthday: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("gender")
    val gender: String,
    @SerializedName("second_name")
    val secondName: String,
    @SerializedName("stats")
    val stats: Stats
)