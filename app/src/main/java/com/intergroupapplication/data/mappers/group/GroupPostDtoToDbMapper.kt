package com.intergroupapplication.data.mappers.group

import com.intergroupapplication.data.db.entity.GroupPostDb
import com.intergroupapplication.data.mappers.ReactsMapper
import com.intergroupapplication.data.mappers.UserProfileMapper
import com.intergroupapplication.data.network.dto.GroupPostDto
import javax.inject.Inject

class GroupPostDtoToDbMapper @Inject constructor(
    private val groupMapper: GroupMapper,
    private val reactsMapper: ReactsMapper,
    private val userProfileMapper: UserProfileMapper,
    private val groupPostMapper: GroupPostMapper
) : (GroupPostDto) -> GroupPostDb {

    override fun invoke(groupPost: GroupPostDto): GroupPostDb {
        return GroupPostDb(
            id = groupPost.id,
            activeCommentsCount = groupPost.activeCommentsCount,
            groupInPost = groupMapper.mapDtoToDb(groupPost.groupInPost),
            bells = groupPostMapper.mapDtoToDb(groupPost.bells),
            reacts = reactsMapper.mapDtoToDb(groupPost.reacts),
            images = groupPost.images,
            audios = groupPost.audios,
            videos = groupPost.videos,
            photo = groupPost.photo,
            postText = groupPost.postText,
            date = groupPost.date,
            updated = groupPost.updated,
            commentsCount = groupPost.commentsCount,
            isActive = groupPost.isActive,
            isOffered = groupPost.isOffered,
            idp = groupPost.idp,
            isPinned = groupPost.isPinned,
            pin = groupPost.pin,
            author = userProfileMapper.mapDtoToDb(groupPost.author),
            unreadComments = groupPost.unreadComments
        )
    }
}
