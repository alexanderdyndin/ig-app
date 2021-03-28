package com.intergroupapplication.domain.entity

import com.appodeal.ads.NativeAd

/**
 * Created by abakarmagomedov on 28/08/2018 at project InterGroupApplication.
 */
sealed class CommentEntity {
    data class Comment(val id: String,
                             val text: String,
                             val date: String,
                             val commentOwner: CommentUserEntity?,
                             val answerTo: String?) : CommentEntity() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Comment

            if (id != other.id) return false
            if (text != other.text) return false
            if (date != other.date) return false
            if (commentOwner != other.commentOwner) return false
            if (answerTo != other.answerTo) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + text.hashCode()
            result = 31 * result + date.hashCode()
            result = 31 * result + (commentOwner?.hashCode() ?: 0)
            result = 31 * result + (answerTo?.hashCode() ?: 0)
            return result
        }
    }

    data class AdEntity(val position: Int, val nativeAd: NativeAd?) : CommentEntity() {
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
