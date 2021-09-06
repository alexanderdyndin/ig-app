package com.intergroupapplication.data.network.dto

import com.google.gson.annotations.SerializedName

data class ReactsRequestDto(
    @SerializedName("is_like")
    val isLike: Boolean,
    @SerializedName("is_dislike")
    val isDislike: Boolean
)
