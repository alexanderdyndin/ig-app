package com.intergroupapplication.data.model

data class AddBlackListUserModel(
    val fullName: String,
    val avatar: String,
    val idProfile: String,
    var isAdministrator: Boolean,
    val isOwner: Boolean,
    var isBlocked: Boolean,
    val subscriptionId: String = "",
    var isSelected: Boolean = false
)
