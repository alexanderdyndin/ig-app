package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

data class NewsDto(@SerializedName("results") val news: List<NewsModel>)