package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

data class AdModel (
        //val id: Int = 1,
        @SerializedName("limit_of_ads_groups")
        val limitOfAdsGroups: Int,
        @SerializedName("first_ad_index_groups")
        val firstAdIndexGroups: Int,
        @SerializedName("no_of_data_between_ads_groups")
        val noOfDataBetweenAdsGroups: Int,
        @SerializedName("limit_of_ads_news")
        val limitOfAdsNews: Int,
        @SerializedName("first_ad_index_news")
        val firstAdIndexNews: Int,
        @SerializedName("no_of_data_between_ads_news")
        val noOfDataBetweenAdsNews: Int,
        @SerializedName("limit_of_ads_comments")
        val limitOfAdsComments: Int,
        @SerializedName("first_ad_index_comments")
        val firstAdIndexComments: Int,
        @SerializedName("no_of_data_between_ads_comments")
        val noOfDataBetweenAdsComments: Int
)