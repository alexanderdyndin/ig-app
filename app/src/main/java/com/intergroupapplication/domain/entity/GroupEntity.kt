package com.intergroupapplication.domain.entity

import android.os.Parcelable
import com.appodeal.ads.NativeAd
import kotlinx.android.parcel.Parcelize

/**
 * Created by abakarmagomedov on 02/08/2018 at project InterGroupApplication.
 */
sealed class GroupEntity {
    @Parcelize data class Group(val id: String,
                                   var followersCount: String,
                                   var postsCount: String,
                                   var postsLikes: String,
                                   var postsDislikes: String,
                                   var CommentsCount: String,
                                   val timeBlocked: String?,
                                   val name: String,
                                   val description: String,
                                   val isBlocked: Boolean,
                                   val owner: String,
                                   var isFollowing: Boolean,
                                   val avatar: String?,
                                   val subject: String,
                                   val rules: String,
                                   val isClosed: Boolean,
                                   val ageRestriction: String,
                                   var isSubscribing: Boolean = false
    ): GroupEntity(), Parcelable {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Group

            if (id != other.id) return false
            if (followersCount != other.followersCount) return false
            if (postsCount != other.postsCount) return false
            if (postsLikes != other.postsLikes) return false
            if (postsDislikes != other.postsDislikes) return false
            if (CommentsCount != other.CommentsCount) return false
            if (timeBlocked != other.timeBlocked) return false
            if (name != other.name) return false
            if (description != other.description) return false
            if (isBlocked != other.isBlocked) return false
            if (owner != other.owner) return false
            if (isFollowing != other.isFollowing) return false
            if (avatar != other.avatar) return false
            if (subject != other.subject) return false
            if (rules != other.rules) return false
            if (isClosed != other.isClosed) return false
            if (ageRestriction != other.ageRestriction) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + followersCount.hashCode()
            result = 31 * result + postsCount.hashCode()
            result = 31 * result + postsLikes.hashCode()
            result = 31 * result + postsDislikes.hashCode()
            result = 31 * result + CommentsCount.hashCode()
            result = 31 * result + (timeBlocked?.hashCode() ?: 0)
            result = 31 * result + name.hashCode()
            result = 31 * result + description.hashCode()
            result = 31 * result + isBlocked.hashCode()
            result = 31 * result + owner.hashCode()
            result = 31 * result + isFollowing.hashCode()
            result = 31 * result + (avatar?.hashCode() ?: 0)
            result = 31 * result + subject.hashCode()
            result = 31 * result + rules.hashCode()
            result = 31 * result + isClosed.hashCode()
            result = 31 * result + ageRestriction.hashCode()
            return result
        }
    }

    data class AdEntity(val position: Int, val nativeAd: NativeAd?) : GroupEntity() {
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
