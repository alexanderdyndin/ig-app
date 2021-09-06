package com.intergroupapplication.data.mappers.group

import com.intergroupapplication.data.db.entity.BellsDb
import com.intergroupapplication.data.mappers.MediaMapper
import com.intergroupapplication.data.mappers.ReactsMapper
import com.intergroupapplication.data.mappers.UserProfileMapper
import com.intergroupapplication.data.network.dto.*
import com.intergroupapplication.domain.entity.*
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 29/08/2018 at project InterGroupApplication.
 */
class GroupPostMapper @Inject constructor(
    private val userProfileMapper: UserProfileMapper,
    private val mediaMapper: MediaMapper,
    private val reactsMapper: ReactsMapper,
    private val groupMapper: GroupMapper
) {

    fun mapToDto(from: GroupPostEntity.PostEntity): GroupPostDto {
        return GroupPostDto(
            id = from.id,
            bells = mapToDto(from.bells),
            groupInPost = groupMapper.mapToDto(from.groupInPost),
            postText = from.postText,
            date = from.date,
            updated = from.updated,
            author = userProfileMapper.mapToDataModel(from.author),
            photo = from.photo,
            commentsCount = from.commentsCount,
            unreadComments = from.unreadComments,
            activeCommentsCount = from.activeCommentsCount,
            isActive = from.isActive,
            isOffered = from.isOffered,
            isPinned = from.isPinned,
            pin = from.pin,
            reacts = reactsMapper.mapToDto(from.reacts),
            idp = from.idp,
            images = from.images.map { mediaMapper.mapToDto(it) },
            audios = from.audios.map { mediaMapper.mapToDto(it) },
            videos = from.videos.map { mediaMapper.mapToDto(it) }
        )
    }

    fun mapToDomainEntity(from: GroupPostDto): GroupPostEntity.PostEntity {
        return GroupPostEntity.PostEntity(
            id = from.id,
            bells = mapToDomainEntity(from.bells),
            groupInPost = groupMapper.mapToDomainEntity(from.groupInPost),
            postText = from.postText,
            date = from.date,
            updated = from.updated,
            author = userProfileMapper.mapToDomainEntity(from.author),
            photo = from.photo,
            commentsCount = from.commentsCount,
            unreadComments = from.unreadComments,
            activeCommentsCount = from.activeCommentsCount,
            isPinned = from.isPinned,
            isActive = from.isActive,
            isOffered = from.isOffered,
            pin = from.pin,
            idp = from.idp,
            reacts = reactsMapper.mapToDomainEntity(from.reacts),
            images = from.images.map { mediaMapper.mapToDomainEntity(it) },
            audios = from.audios.map { mediaMapper.mapToDomainEntity(it) },
            videos = from.videos.map { mediaMapper.mapToDomainEntity(it) }
        )
    }

    fun mapToPostEntity(from: GroupPostDto): CommentEntity.PostEntity {
        return CommentEntity.PostEntity(
            id = from.id,
            bells = mapToDomainEntity(from.bells),
            groupInPost = groupMapper.mapToDomainEntity(from.groupInPost),
            postText = from.postText,
            date = from.date,
            updated = from.updated,
            author = userProfileMapper.mapToDomainEntity(from.author),
            photo = from.photo,
            commentsCount = from.commentsCount,
            unreadComments = from.unreadComments,
            activeCommentsCount = from.activeCommentsCount,
            isPinned = from.isPinned,
            isActive = from.isActive,
            isOffered = from.isOffered,
            pin = from.pin,
            idp = from.idp,
            reacts = reactsMapper.mapToDomainEntity(from.reacts),
            images = from.images.map { mediaMapper.mapToDomainEntity(it) },
            audios = from.audios.map { mediaMapper.mapToDomainEntity(it) },
            videos = from.videos.map { mediaMapper.mapToDomainEntity(it) }
        )
    }

    fun mapNewsListToDomainEntity(from: NewsDto): NewsPostsEntity {
        return NewsPostsEntity(from.count.toInt(), from.next,
            from.previous, from.newsPosts.map { mapToDomainEntity(it) })
    }


    fun mapToDto(from: CreateGroupPostEntity): CreateGroupPostDto {
        return CreateGroupPostDto(
            postText = from.postText,
            images = from.images.map { mediaMapper.mapToDto(it) },
            audios = from.audios.map { mediaMapper.mapToDto(it) },
            videos = from.videos.map { mediaMapper.mapToDto(it) },
            isPinned = from.isPinned,
            pinTime = from.pinTime
        )
    }

    fun mapToDomainEntity(from: CreateGroupPostDto): CreateGroupPostEntity =
        CreateGroupPostEntity(
            postText = from.postText,
            images = from.images.map { mediaMapper.mapToDomainEntity(it) },
            audios = from.audios.map { mediaMapper.mapToDomainEntity(it) },
            videos = from.videos.map { mediaMapper.mapToDomainEntity(it) },
            isPinned = from.isPinned,
            pinTime = from.pinTime
        )

    fun mapListToDomainEntity(from: List<GroupPostDto>): List<GroupPostEntity> =
        from.map { mapToDomainEntity(it) }


    fun mapToDomainEntity(from: GroupPostsDto): GroupPostsEntity {
        return GroupPostsEntity(from.count.toInt(), from.next,
            from.previous, from.posts.map { mapToDomainEntity(it) })
    }


    fun mapToDto(from: BellsEntity): BellsDto =
        BellsDto(
            count = from.count,
            isActive = from.isActive
        )

    fun mapDbToEntity(from: BellsDb): BellsEntity =
        BellsEntity(
            count = from.count,
            isActive = from.isActive
        )

    fun mapDtoToDb(from: BellsDto): BellsDb =
        BellsDb(
            count = from.count,
            isActive = from.isActive
        )

    fun mapToDomainEntity(from: BellsDto): BellsEntity =
        BellsEntity(
            count = from.count,
            isActive = from.isActive
        )

    fun mapToDto(from: NewsEntity.Post): NewsPostDto =
        NewsPostDto(
            id = from.id,
            post = mapToDto(from.post),
            user = from.user
        )

    fun mapToDomainEntity(from: NewsPostDto): NewsEntity =
        NewsEntity.Post(
            id = from.id,
            post = mapToDomainEntity(from.post),
            user = from.user
        )

    fun mapToDto(from: BellFollowEntity): BellFollowDto =
        BellFollowDto(
            from.id,
            from.user,
            from.post
        )

    fun mapToDomainEntity(from: BellFollowDto): BellFollowEntity =
        BellFollowEntity(
            from.id,
            from.user,
            from.post
        )
}
