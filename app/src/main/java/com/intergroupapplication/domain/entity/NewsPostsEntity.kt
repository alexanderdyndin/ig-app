package com.intergroupapplication.domain.entity


data class NewsPostsEntity(
        val count: Int,
        val next: Int?,
        val previous: Int?,
        val news: List<NewsEntity>
)