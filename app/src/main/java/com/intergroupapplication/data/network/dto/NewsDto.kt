package com.intergroupapplication.data.network.dto

import com.google.gson.annotations.SerializedName

data class NewsDto(
    val count: String,
    val next: String?,
    val previous: String?,
    @SerializedName("results")
    val newsPosts: List<NewsPostDto>
)
