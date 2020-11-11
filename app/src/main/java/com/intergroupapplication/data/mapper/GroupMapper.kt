package com.intergroupapplication.data.mapper

import com.intergroupapplication.data.model.GroupModel
import com.intergroupapplication.domain.entity.GroupEntity
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 28/08/2018 at project InterGroupApplication.
 */
class GroupMapper @Inject constructor() {

    fun mapToDto(from: GroupEntity): GroupModel {
        return GroupModel(
                id = from.id,
                followersCount = from.followersCount,
                postsCount = from.postsCount,
                postsLikes = from.postsLikes,
                postsDislikes = from.postsDislikes,
                CommentsCount = from.CommentsCount,
                timeBlocked = from.timeBlocked,
                name = from.name,
                description = from.description,
                isBlocked = from.isBlocked,
                owner = from.owner,
                isFollowing = from.isFollowing,
                avatar = from.avatar,
                subject = from.subject,
                rules = from.rules,
                isClosed = from.isClosed,
                ageRestriction = from.ageRestriction.replace("+","", true))
    }

    fun mapToDomainEntity(from: GroupModel): GroupEntity {
        return GroupEntity(
                id = from.id,
                followersCount = from.followersCount,
                postsCount = from.postsCount,
                postsLikes = from.postsLikes,
                postsDislikes = from.postsDislikes,
                CommentsCount = from.CommentsCount,
                timeBlocked = from.timeBlocked,
                name = from.name,
                description = from.description,
                isBlocked = from.isBlocked,
                owner = from.owner,
                isFollowing = from.isFollowing,
                avatar = from.avatar,
                subject = from.subject,
                rules = from.rules,
                isClosed = from.isClosed,
                ageRestriction =  "${from.ageRestriction}+" )
    }

    fun mapListToDomainEntity(from: List<GroupModel>): List<GroupEntity> =
            from.map { mapToDomainEntity(it) }
}