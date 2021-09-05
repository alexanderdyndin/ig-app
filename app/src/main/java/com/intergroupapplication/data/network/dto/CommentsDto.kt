package com.intergroupapplication.data.network.dto

import com.google.gson.annotations.SerializedName
import com.intergroupapplication.data.model.CommentModel

data class CommentsDto(
    val count: String,
    val next: String?,
    val previous: String?,
    @SerializedName("results")
    val comments: List<CommentModel>
)
