package com.intergroupapplication.data.mapper

import com.intergroupapplication.data.model.*
import com.intergroupapplication.domain.entity.*
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 29/08/2018 at project InterGroupApplication.
 */
class GroupPostMapper @Inject constructor(private val groupInPostMapper: GroupInPostMapper,
                                          private val userProfileMapper: UserProfileMapper,
                                          private val mediaMapper: MediaMapper) {

    fun mapToDto(from: GroupPostEntity.PostEntity): GroupPostModel {
        return GroupPostModel(
                id = from.id,
                bells = mapToDto(from.bells),
                groupInPost = groupInPostMapper.mapToDto(from.groupInPost),
                postText = from.postText,
                date = from.date,
                updated = from.updated,
                author = userProfileMapper.mapToDataModel(from.author),
                photo = from.photo,
                commentsCount = from.commentsCount,
                activeCommentsCount = from.activeCommentsCount,
                isActive = from.isActive,
                isOffered = from.isOffered,
                isPinned = from.isPinned,
                pin = from.pin,
                reacts = mapToDto(from.reacts),
                idp = from.idp,
                images = from.images.map { mediaMapper.mapToDto(it) },
                audios = from.audios.map { mediaMapper.mapToDto(it) },
                videos = from.videos.map { mediaMapper.mapToDto(it) }
        )
    }

    fun mapToDomainEntity(from: GroupPostModel): GroupPostEntity.PostEntity {
        return GroupPostEntity.PostEntity(
                id = from.id,
                bells = mapToDomainEntity(from.bells),
                groupInPost = groupInPostMapper.mapToDomainEntity(from.groupInPost),
                postText = from.postText,
                date = from.date,
                updated = from.updated,
                author = userProfileMapper.mapToDomainEntity(from.author),
                photo = from.photo,
                commentsCount = from.commentsCount,
                activeCommentsCount = from.activeCommentsCount,
                isPinned = from.isPinned,
                isActive = from.isActive,
                isOffered = from.isOffered,
                pin = from.pin,
                idp = from.idp,
                reacts = mapToDomainEntity(from.reacts),
                images = from.images.map { mediaMapper.mapToDomainEntity(it) },
                audios = from.audios.map { mediaMapper.mapToDomainEntity(it) },
                videos = from.videos.map { mediaMapper.mapToDomainEntity(it) }
        )
    }


    fun mapNewsListToDomainEntity(from: NewsDto): NewsEntity {
        val regex = Regex(".*page=")
        val previous = if (from.previous == "http://backend-v2:8080/groups/news/") { //TODO попросить пофиксить бэкэнд
            1
        } else {
            from.previous?.replace(regex, "")?.toInt()
        }
        return NewsEntity(from.count.toInt(), from.next?.replace(regex,"")?.toInt(),
                previous, from.news.map { mapToDomainEntity(it.post) })
    }



    fun mapToDto(from: CreateGroupPostEntity): CreateGroupPostModel {
        return CreateGroupPostModel(
                postText = from.postText,
                images = from.images.map { mediaMapper.mapToDto(it) },
                audios = from.audios.map { mediaMapper.mapToDto(it) },
                videos = from.videos.map { mediaMapper.mapToDto(it) },
                isPinned = from.isPinned,
                pinTime = from.pinTime
        )
    }

    fun mapToDomainEntity(from: CreateGroupPostModel): CreateGroupPostEntity =
            CreateGroupPostEntity(
                    postText = from.postText,
                    images = from.images.map { mediaMapper.mapToDomainEntity(it) },
                    audios = from.audios.map { mediaMapper.mapToDomainEntity(it) },
                    videos = from.videos.map { mediaMapper.mapToDomainEntity(it) },
                    isPinned = from.isPinned,
                    pinTime = from.pinTime
                    )

    fun mapListToDomainEntity(from: List<GroupPostModel>): List<GroupPostEntity> =
            from.map { mapToDomainEntity(it) }


    fun mapToDto(from: ReactsEntityRequest): ReactsModelRequest =
            ReactsModelRequest(
                    isLike = from.isLike,
                    isDislike = from.isDislike
            )

    fun mapToDomainEntity(from: ReactsModelRequest): ReactsEntityRequest =
            ReactsEntityRequest(
                    isLike = from.isLike,
                    isDislike = from.isDislike
            )

    fun mapToDomainEntity(from: GroupPostsDto): GroupPostsEntity {
        return GroupPostsEntity(from.count.toInt(), from.next,
                from.previous, from.results.map { mapToDomainEntity(it) })
    }

    fun mapToDto(from: ReactsEntity): ReactsModel =
            ReactsModel(
                    isLike = from.isLike,
                    isDislike = from.isDislike,
                    likesCount = from.likesCount,
                    dislikesCount = from.dislikesCount
            )

    fun mapToDomainEntity(from: ReactsModel): ReactsEntity =
            ReactsEntity(
                    isLike = from.isLike,
                    isDislike = from.isDislike,
                    likesCount = from.likesCount,
                    dislikesCount = from.dislikesCount
            )

    fun mapToDto(from: BellsEntity): BellsModel =
            BellsModel(
                    count = from.count,
                    isActive = from.isActive
            )

    fun mapToDomainEntity(from: BellsModel): BellsEntity =
            BellsEntity(
                    count = from.count,
                    isActive = from.isActive
            )
}
