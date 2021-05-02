package com.intergroupapplication.data.mapper

import com.intergroupapplication.data.model.group_followers.GroupUserFollowersDto
import com.intergroupapplication.domain.entity.GroupFollowersListEntity
import com.intergroupapplication.domain.entity.GroupUserEntity
import javax.inject.Inject

class FollowersGroupMapper @Inject constructor() {
    fun mapToDomainEntity(from: GroupUserFollowersDto): GroupFollowersListEntity {
        return GroupFollowersListEntity(
                count = from.count,
                next = from.next,
                previous = from.previous,
                users = from.results.map { result ->
                    result.user.run {
                        val isOwner = result.owner ?: false
                        val isAdmin = result.isAdmin ?: false || isOwner
                        GroupUserEntity(
                                firstName = profile.firstName,
                                surName = profile.secondName,
                                avatar = profile.avatar ?: "",
                                idProfile = id.toString(),
                                commentsCount = profile.stats.comments,
                                likesCount = profile.stats.likes,
                                dislikeCount = profile.stats.dislikes,
                                postsCount = profile.stats.posts,
                                isAdministrator = isAdmin,
                                isOwner = isOwner,
                                isBlocked = false,
                                subscriptionId = result.id.toString()
                        )
                    }
                }
        )
    }
}