package com.intergroupapplication.data.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Created by abakarmagomedov on 19/09/2018 at project InterGroupApplication.
 */
data class GroupPostsDto(
    val count: String,
    val next: String?,
    val previous: String?,
    @SerializedName("results")
    val posts: List<GroupPostDto>
)
