package com.intergroupapplication.data.model.group_user_followers


import com.google.gson.annotations.SerializedName

data class Result(
    @SerializedName("admin_permissions")
    val adminPermissions: Int?,
    @SerializedName("group")
    val group: Int,
    @SerializedName("id")
    val id: Int,
    @SerializedName("is_admin")
    val isAdmin: Boolean?,
    @SerializedName("owner")
    val owner: String?,
    @SerializedName("user")
    val user: User
)