package com.intergroupapplication.data.mapper

import com.intergroupapplication.data.model.GroupFollowModel
import com.intergroupapplication.data.model.GroupModel
import com.intergroupapplication.data.model.GroupsDto
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.domain.entity.GroupFollowEntity
import com.intergroupapplication.domain.entity.GroupListEntity
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

    fun mapToDomainEntity(from: GroupsDto): GroupListEntity {
        val regex = Regex(pattern = "=\\d*&")
        val previous = when (from.previous) {
            "http://backend-v2:8080/groups/?search=" -> {
                1
            }
            null -> {
                null
            }
            else -> {
                regex.find(from.previous)?.value?.replace("=","")?.replace("&", "")?.toInt()
            }
        }
        val next = if (from.next == null) {
            null
        } else {
            regex.find(from.next)?.value?.replace("=","")?.replace("&", "")?.toInt()
        }
        return GroupListEntity(
                count = from.count.toInt(),
                next,
                previous,
                mapListToDomainEntity(from.groups))
    }

    fun followsToModel(from: GroupFollowEntity): GroupFollowModel {
        return GroupFollowModel(from.id, from.is_blocked, from.time_blocked, from.user, from.group)
    }

    fun followsToEntity(from: GroupFollowModel): GroupFollowEntity {
        return GroupFollowEntity(from.id, from.is_blocked, from.time_blocked, from.user, from.group)
    }
}