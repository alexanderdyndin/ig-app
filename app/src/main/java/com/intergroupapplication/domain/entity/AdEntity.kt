package com.intergroupapplication.domain.entity

data class AdEntity (val limitOfAdsGroups: Int,
                     val FirstAdIndexGroups: Int,
                     val noOfDataBetweenAdsGroups: Int,
                     val limitOfAdsNews: Int,
                     val FirstAdIndexNews: Int,
                     val noOfDataBetweenAdsNews: Int,
                     val limitOfAdsComments: Int,
                     val FirstAdIndexComments: Int,
                     val noOfDataBetweenAdsComments: Int)