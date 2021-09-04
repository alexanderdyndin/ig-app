package com.intergroupapplication.data.mapper.group

import com.intergroupapplication.data.db.entity.GroupDb
import com.intergroupapplication.data.model.GroupFollowModel
import com.intergroupapplication.data.network.dto.GroupDto
import com.intergroupapplication.data.network.dto.GroupsDto
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.domain.entity.GroupFollowEntity
import com.intergroupapplication.domain.entity.GroupListEntity
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 28/08/2018 at project InterGroupApplication.
 */
class GroupMapper @Inject constructor() {

    fun mapToDto(from: GroupEntity.Group): GroupDto {
        return GroupDto(
            id = from.id,
            followersCount = from.followersCount,
            postsCount = from.postsCount,
            postsLikes = from.postsLikes,
            postsDislikes = from.postsDislikes,
            commentsCount = from.CommentsCount,
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
            ageRestriction = from.ageRestriction.replace("+", "", true)
        )
    }

    fun mapToDomainEntity(from: GroupDto): GroupEntity.Group {
        return GroupEntity.Group(
            id = from.id,
            followersCount = from.followersCount,
            postsCount = from.postsCount,
            postsLikes = from.postsLikes,
            postsDislikes = from.postsDislikes,
            CommentsCount = from.commentsCount,
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
            ageRestriction = "${from.ageRestriction}+"
        )
    }

    fun mapDbToEntity(from: GroupDb): GroupEntity.Group {
        return GroupEntity.Group(
            id = from.id,
            followersCount = from.followersCount,
            postsCount = from.postsCount,
            postsLikes = from.postsLikes,
            postsDislikes = from.postsDislikes,
            CommentsCount = from.commentsCount,
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
            ageRestriction = "${from.ageRestriction}+"
        )
    }

    fun mapDtoToDb(group: GroupDto): GroupDb {
        return GroupDb(
            id = group.id,
            avatar = group.avatar,
            followersCount = group.followersCount,
            isFollowing = group.isFollowing,
            postsCount = group.postsCount,
            postsLikes = group.postsLikes,
            postsDislikes = group.postsDislikes,
            commentsCount = group.commentsCount,
            name = group.name,
            description = group.description,
            rules = group.rules,
            isBlocked = group.isBlocked,
            timeBlocked = group.timeBlocked,
            isClosed = group.isClosed,
            ageRestriction = group.ageRestriction,
            subject = group.subject,
            owner = group.owner
        )
    }

    fun mapDbToDto(group: GroupDb): GroupDto {
        return GroupDto(
            id = group.id,
            avatar = group.avatar,
            followersCount = group.followersCount,
            isFollowing = group.isFollowing,
            postsCount = group.postsCount,
            postsLikes = group.postsLikes,
            postsDislikes = group.postsDislikes,
            commentsCount = group.commentsCount,
            name = group.name,
            description = group.description,
            rules = group.rules,
            isBlocked = group.isBlocked,
            timeBlocked = group.timeBlocked,
            isClosed = group.isClosed,
            ageRestriction = group.ageRestriction,
            subject = group.subject,
            owner = group.owner
        )
    }

    fun mapListToDomainEntity(from: List<GroupDto>): List<GroupEntity.Group> =
        from.map { mapToDomainEntity(it) }

    fun mapToDomainEntity(from: GroupsDto): GroupListEntity {
        return GroupListEntity(
            count = from.count.toInt(),
            next = from.next,
            previous = from.previous,
            groups = mapListToDomainEntity(from.groups)
        )
    }

    fun followsToModel(from: GroupFollowEntity): GroupFollowModel {
        return GroupFollowModel(from.id, from.is_blocked, from.time_blocked, from.user, from.group)
    }

    fun followsToEntity(from: GroupFollowModel): GroupFollowEntity {
        return GroupFollowEntity(from.id, from.is_blocked, from.time_blocked, from.user, from.group)
    }
}