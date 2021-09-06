package com.intergroupapplication.data.mappers.group

import com.intergroupapplication.data.db.entity.GroupPostDb
import com.intergroupapplication.data.mappers.MediaMapper
import com.intergroupapplication.data.mappers.ReactsMapper
import com.intergroupapplication.data.mappers.UserProfileMapper
import com.intergroupapplication.domain.entity.GroupPostEntity
import javax.inject.Inject

class GroupPostDbToEntityMapper @Inject constructor(
    private val groupMapper: GroupMapper,
    private val userProfileMapper: UserProfileMapper,
    private val mediaMapper: MediaMapper,
    private val reactsMapper: ReactsMapper,
    private val groupPostMapper: GroupPostMapper,
) : (GroupPostDb) -> GroupPostEntity.PostEntity {

    override fun invoke(groupPost: GroupPostDb): GroupPostEntity.PostEntity {
        return GroupPostEntity.PostEntity(
            id = groupPost.id,
            bells = groupPostMapper.mapDbToEntity(groupPost.bells),
            groupInPost = groupMapper.mapDbToEntity(groupPost.groupInPost),
            postText = groupPost.postText,
            date = groupPost.date,
            updated = groupPost.updated,
            author = userProfileMapper.mapDbToEntity(groupPost.author),
            pin = groupPost.pin,
            photo = groupPost.photo,
            commentsCount = groupPost.commentsCount,
            activeCommentsCount = groupPost.activeCommentsCount,
            isActive = groupPost.isActive,
            isOffered = groupPost.isOffered,
            isPinned = groupPost.isPinned,
            reacts = reactsMapper.mapDbToEntity(groupPost.reacts),
            idp = groupPost.idp,
            images = groupPost.images.map { mediaMapper.mapToDomainEntity(it) },
            audios = groupPost.audios.map { mediaMapper.mapToDomainEntity(it) },
            videos = groupPost.videos.map { mediaMapper.mapToDomainEntity(it) },
            unreadComments = groupPost.unreadComments
        )
    }
}
