package com.intergroupapplication.data.model.group_user_followers


import com.google.gson.annotations.SerializedName

data class Result(
    @SerializedName("group")
    val group: Int,
    @SerializedName("id")
    val id: Int,
    @SerializedName("user")
    val user: User
)