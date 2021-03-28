package com.intergroupapplication.domain.entity

import com.google.gson.annotations.SerializedName
import com.intergroupapplication.data.model.NewsModel

data class NewsEntity(
        val count: Int,
        val next: Int?,
        val previous: Int?,
        @SerializedName("results") val news: List<GroupPostEntity.PostEntity>
)