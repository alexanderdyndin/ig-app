package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName
import com.intergroupapplication.domain.entity.CommentEntity

data class CommentsDto(@SerializedName("results") val comments: List<CommentModel>)