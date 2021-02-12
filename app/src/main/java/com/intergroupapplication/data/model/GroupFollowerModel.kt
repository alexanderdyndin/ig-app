package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

data class GroupFollowerModel(
        val id: Int,
        @SerializedName("is_admin") val isAdmin: Boolean?,
        @SerializedName("admin_permissions") val permissions: Int?,
        val user: UserModel,
        val group: Int,
        val owner: Boolean?
)