package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

data class ReactsModelRequest(
        @SerializedName("is_like")
        val isLike: Boolean,
        @SerializedName("is_dislike")
        val isDislike: Boolean
)
