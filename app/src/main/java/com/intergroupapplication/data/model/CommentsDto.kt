package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName
import com.intergroupapplication.domain.entity.CommentEntity

data class CommentsDto(
        val count: String,
        val next: String?,
        val previous: String?,
        @SerializedName("results") val comments: List<CommentModel>
        )