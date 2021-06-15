package com.intergroupapplication.domain.entity

import android.os.Parcelable
import com.appodeal.ads.NativeAd
import kotlinx.android.parcel.Parcelize

/**
 * Created by abakarmagomedov on 28/08/2018 at project InterGroupApplication.
 */
sealed class CommentEntity {
    @Parcelize
    data class Comment(val id: String,
                       val commentOwner: CommentUserEntity?,
                       var reacts: ReactsEntity,
                       val images: List<FileEntity>,
                       val audios: List<AudioEntity>,
                       val videos: List<FileEntity>,
                       val text: String,
                       val date: String,
                       val isActive: Boolean,
                       val idc: Int,
                       val post: Int,
                       val answerTo: Comment?) : CommentEntity(),Parcelable {
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
            if (nativeAd != other.nativeAd) return false

            return true
        }

        override fun hashCode(): Int {
            var result = position
            result = 31 * result + (nativeAd?.hashCode() ?: 0)
            return result
        }

    }

    data class PostEntity(
            val id: String,
            val bells: BellsEntity,
            val groupInPost: GroupInPostEntity,
            val postText: String,
            val date: String,
            val updated: String?,
            val author: AuthorEntity,
            val pin: String?,
            val photo: String?,
            var commentsCount: String,
            val activeCommentsCount: String,
            val isActive: Boolean,
            val isOffered: Boolean,
            var isPinned: Boolean,
            var reacts: ReactsEntity,
            val idp: Int,
            val images: List<FileEntity>,
            val audios: List<AudioEntity>,
            val videos: List<FileEntity>,
            var isLoading: Boolean = false,
            val unreadComments: String,
            var imagesExpanded: Boolean = false,
            var audiosExpanded: Boolean = false,
            var videosExpanded: Boolean = false
    ):CommentEntity()
}
