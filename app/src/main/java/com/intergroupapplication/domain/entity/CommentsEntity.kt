package com.intergroupapplication.domain.entity

import com.google.gson.annotations.SerializedName
import com.intergroupapplication.domain.entity.CommentEntity

data class CommentsEntity(
        val count: Int,
        val next: String?,
        val previous: String?,
        val comments: List<CommentEntity>
        )