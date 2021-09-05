package com.intergroupapplication.data.model.groupfollowers


import com.google.gson.annotations.SerializedName

data class UpdateGroupAdmin(
    @SerializedName("is_admin")
    val isAdmin: Boolean
)
