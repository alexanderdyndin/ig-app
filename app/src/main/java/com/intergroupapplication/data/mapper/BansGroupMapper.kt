package com.intergroupapplication.data.mapper

import com.intergroupapplication.data.network.dto.GroupUserBansDto
import com.intergroupapplication.domain.entity.GroupFollowersListEntity
import com.intergroupapplication.domain.entity.GroupUserEntity
import javax.inject.Inject

class BansGroupMapper @Inject constructor() {
    fun mapToDomainEntity(from: GroupUserBansDto): GroupFollowersListEntity {
        return GroupFollowersListEntity(
                count = from.count,
                next = from.next,
                previous = from.previous,
                users = from.results.map { result ->
                    result.user.run {
                        GroupUserEntity(
                                firstName = profile.firstName,
                                surName = profile.secondName,
                                avatar = profile.avatar ?: "",
                                idProfile = id.toString(),
                                commentsCount = profile.stats.comments,
                                likesCount = profile.stats.likes,
                                dislikeCount = profile.stats.dislikes,
                                postsCount = profile.stats.posts,
                                isAdministrator = false,
                                isOwner = false,
                                isBlocked = true,
                                banId = result.id.toString()
                        )
                    }
                }
        )
    }
}