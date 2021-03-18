package com.intergroupapplication.presentation.feature.news.other

import com.appodeal.ads.NativeAd
import com.intergroupapplication.domain.entity.*

/**
 * Created by abakarmagomedov on 27/08/2018 at project InterGroupApplication.
 */

sealed class GroupPostEntityUI {
        data class GroupPostEntity(
                val id: String,
                val groupInPost: GroupInPostEntity,
                val postText: String,
                val date: String,
                val updated: String?,
                val author: AuthorEntity,
                val unreadComments: Int,
                val pin: String?,
                val photo: String?,
                var commentsCount: String,
                val activeCommentsCount: String,
                val isActive: Boolean,
                val isOffered: Boolean,
                var reacts: ReactsEntity,
                val images: List<FileEntity>,
                val audios: List<AudioEntity>,
                val videos: List<FileEntity>,
                var isLoading: Boolean = false,
                var imagesExpanded: Boolean = false
        ) : GroupPostEntityUI() {
                override fun equals(other: Any?): Boolean {
                        if (this === other) return true
                        if (javaClass != other?.javaClass) return false

                        other as GroupPostEntity

                        if (id != other.id) return false
                        if (groupInPost != other.groupInPost) return false
                        if (postText != other.postText) return false
                        if (date != other.date) return false
                        if (updated != other.updated) return false
                        if (author != other.author) return false
                        if (unreadComments != other.unreadComments) return false
                        if (pin != other.pin) return false
                        if (photo != other.photo) return false
                        if (commentsCount != other.commentsCount) return false
                        if (activeCommentsCount != other.activeCommentsCount) return false
                        if (isActive != other.isActive) return false
                        if (isOffered != other.isOffered) return false
                        if (images != other.images) return false
                        if (audios != other.audios) return false
                        if (videos != other.videos) return false
                        return true
                }

                override fun hashCode(): Int {
                        var result = id.hashCode()
                        result = 31 * result + groupInPost.hashCode()
                        result = 31 * result + postText.hashCode()
                        result = 31 * result + date.hashCode()
                        result = 31 * result + (updated?.hashCode() ?: 0)
                        result = 31 * result + author.hashCode()
                        result = 31 * result + unreadComments
                        result = 31 * result + (pin?.hashCode() ?: 0)
                        result = 31 * result + (photo?.hashCode() ?: 0)
                        result = 31 * result + commentsCount.hashCode()
                        result = 31 * result + activeCommentsCount.hashCode()
                        result = 31 * result + isActive.hashCode()
                        result = 31 * result + isOffered.hashCode()
                        result = 31 * result + images.hashCode()
                        result = 31 * result + audios.hashCode()
                        result = 31 * result + videos.hashCode()
                        return result
                }
        }

        data class AdEntity(val position: Int, val nativeAd: NativeAd?) : GroupPostEntityUI() {
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