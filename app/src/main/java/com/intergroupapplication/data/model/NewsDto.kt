package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

data class NewsDto(
        val count: String,
        val next: String?,
        val previous: String?,
        @SerializedName("results") val news: List<NewsModel>)