package com.intergroupapplication.data.model.group_user_followers


import com.google.gson.annotations.SerializedName

data class GroupUserFollowersDto(
    @SerializedName("count")
    val count: Int,
    @SerializedName("next")
    val next: String?,
    @SerializedName("previous")
    val previous: String?,
    @SerializedName("results")
    var results: List<Result>
)