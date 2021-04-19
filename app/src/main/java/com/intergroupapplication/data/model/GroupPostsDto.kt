package com.intergroupapplication.data.model

/**
 * Created by abakarmagomedov on 19/09/2018 at project InterGroupApplication.
 */
data class GroupPostsDto(
        val count: String,
        val next: String,
        val previous: String,
        val results: List<GroupPostModel>
)