package com.intergroupapplication.data.model.groupfollowers


import com.google.gson.annotations.SerializedName

data class Profile(
    val avatar: String?,
    val birthday: String,
    @SerializedName("first_name")
    val firstName: String,
    val gender: String,
    @SerializedName("second_name")
    val secondName: String,
    val stats: Stats
)
