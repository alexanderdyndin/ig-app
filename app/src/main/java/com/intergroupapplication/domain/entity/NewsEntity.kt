package com.intergroupapplication.domain.entity

import com.appodeal.ads.NativeAd

sealed class NewsEntity {

    data class Post(
            val id: Int,
            val post: GroupPostEntity.PostEntity,
            val user: Int
    ): NewsEntity()

    data class AdEntity(val position: Int, val nativeAd: NativeAd?) : NewsEntity() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as AdEntity

            if (position != other.position) return false

            return true
        }

        override fun hashCode(): Int {
            return position
        }
    }
}
