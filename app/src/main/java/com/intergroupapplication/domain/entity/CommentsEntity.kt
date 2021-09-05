package com.intergroupapplication.domain.entity

data class CommentsEntity(
    val count: Int,
    val next: String?,
    val previous: String?,
    val comments: List<CommentEntity.Comment>
)
