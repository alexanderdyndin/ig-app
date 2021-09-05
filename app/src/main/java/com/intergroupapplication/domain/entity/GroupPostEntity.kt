package com.intergroupapplication.domain.entity

import android.os.Parcelable
import com.appodeal.ads.NativeAd
import kotlinx.parcelize.Parcelize

/**
 * Created by abakarmagomedov on 27/08/2018 at project InterGroupApplication.
 */

sealed class GroupPostEntity {
    @Parcelize
    data class PostEntity(
        val id: String,
        val bells: BellsEntity,
        val groupInPost: GroupEntity.Group,
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
    ) : GroupPostEntity(), Parcelable {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as PostEntity

            if (id != other.id) return false
            if (bells != other.bells) return false
            if (groupInPost != other.groupInPost) return false
            if (postText != other.postText) return false
            if (date != other.date) return false
            if (updated != other.updated) return false
            if (author != other.author) return false
            if (pin != other.pin) return false
            if (photo != other.photo) return false
            if (commentsCount != other.commentsCount) return false
            if (activeCommentsCount != other.activeCommentsCount) return false
            if (isActive != other.isActive) return false
            if (isOffered != other.isOffered) return false
            if (isPinned != other.isPinned) return false
            if (reacts != other.reacts) return false
            if (idp != other.idp) return false
            if (images != other.images) return false
            if (audios != other.audios) return false
            if (videos != other.videos) return false
            if (isLoading != other.isLoading) return false
            if (unreadComments != other.unreadComments) return false
            if (imagesExpanded != other.imagesExpanded) return false
            if (audiosExpanded != other.audiosExpanded) return false
            if (videosExpanded != other.videosExpanded) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + bells.hashCode()
            result = 31 * result + groupInPost.hashCode()
            result = 31 * result + postText.hashCode()
            result = 31 * result + date.hashCode()
            result = 31 * result + (updated?.hashCode() ?: 0)
            result = 31 * result + author.hashCode()
            result = 31 * result + (pin?.hashCode() ?: 0)
            result = 31 * result + (photo?.hashCode() ?: 0)
            result = 31 * result + commentsCount.hashCode()
            result = 31 * result + activeCommentsCount.hashCode()
            result = 31 * result + isActive.hashCode()
            result = 31 * result + isOffered.hashCode()
            result = 31 * result + isPinned.hashCode()
            result = 31 * result + reacts.hashCode()
            result = 31 * result + idp
            result = 31 * result + images.hashCode()
            result = 31 * result + audios.hashCode()
            result = 31 * result + videos.hashCode()
            result = 31 * result + isLoading.hashCode()
            result = 31 * result + unreadComments.hashCode()
            result = 31 * result + imagesExpanded.hashCode()
            result = 31 * result + audiosExpanded.hashCode()
            result = 31 * result + videosExpanded.hashCode()
            return result
        }

    }

    data class AdEntity(val position: Int, val nativeAd: NativeAd?) : GroupPostEntity() {
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
