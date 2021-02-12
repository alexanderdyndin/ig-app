package com.intergroupapplication.domain.entity

data class AdEntity (
        val limitOfAdsGroups: Int,
        val firstAdIndexGroups: Int,
        val noOfDataBetweenAdsGroups: Int,
        val limitOfAdsNews: Int,
        val firstAdIndexNews: Int,
        val noOfDataBetweenAdsNews: Int,
        val limitOfAdsComments: Int,
        val firstAdIndexComments: Int,
        val noOfDataBetweenAdsComments: Int
        )
