package com.intergroupapplication.data.mapper

import com.intergroupapplication.data.model.group_user_followers.GroupUserFollowersDto
import com.intergroupapplication.domain.entity.GroupFollowersListEntity
import com.intergroupapplication.domain.entity.UserEntity
import javax.inject.Inject

class FollowersGroupMapper @Inject constructor() {
    fun mapToDomainEntity(from: GroupUserFollowersDto): GroupFollowersListEntity {
        return GroupFollowersListEntity(
                count = from.count,
                next = from.next,
                previous = from.previous,
                users = from.results.map {
                    UserEntity(
                            id = it.user.id.toString(),
                            firstName = it.user.profile.firstName,
                            surName = it.user.profile.secondName,
                            birthday = it.user.profile.birthday,
                            gender = it.user.profile.gender,
                            email = it.user.email,
                            isBlocked = it.user.isBlocked,
                            isActive = it.user.isVerified,
                            avatar = it.user.profile.avatar
                    )
                }
        )
    }
}