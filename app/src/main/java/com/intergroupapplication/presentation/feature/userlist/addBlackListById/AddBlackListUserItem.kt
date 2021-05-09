package com.intergroupapplication.presentation.feature.userlist.addBlackListById

data class AddBlackListUserItem(
        val fullName: String,
        val avatar: String,
        val idProfile: String,
        var isAdministrator: Boolean,
        val isOwner: Boolean,
        var isBlocked: Boolean,
        val subscriptionId: String = "",
        var isSelected: Boolean = false
)