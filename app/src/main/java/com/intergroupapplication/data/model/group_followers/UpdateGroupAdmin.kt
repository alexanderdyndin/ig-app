package com.intergroupapplication.data.model.group_followers


import com.google.gson.annotations.SerializedName

data class UpdateGroupAdmin(
    @SerializedName("is_admin")
    val isAdmin: Boolean
)