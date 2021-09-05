package com.intergroupapplication.data.network.dto

import com.google.gson.annotations.SerializedName

data class GroupsDto(
    val count: String,
    val next: String?,
    val previous: String?,
    @SerializedName("results") val groups: List<GroupDto>
)