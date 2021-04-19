package com.intergroupapplication.domain.entity


data class NewsPostsEntity(
        val count: Int,
        val next: String?,
        val previous: String?,
        val news: List<NewsEntity>
)