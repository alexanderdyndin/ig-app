package com.intergroupapplication.data.model.group_user_followers


import com.google.gson.annotations.SerializedName

data class Result(
    @SerializedName("admin_permissions")
    val adminPermissions: Int?,
    val group: Int,
    val id: Int,
    @SerializedName("is_admin")
    val isAdmin: Boolean?,
    val owner: String?,
    val user: User
)