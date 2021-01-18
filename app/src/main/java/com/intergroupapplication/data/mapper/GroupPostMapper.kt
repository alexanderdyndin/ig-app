package com.intergroupapplication.data.mapper

import com.intergroupapplication.data.model.*
import com.intergroupapplication.domain.entity.*
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 29/08/2018 at project InterGroupApplication.
 */
class GroupPostMapper @Inject constructor(val groupInPostMapper: GroupInPostMapper) {

    fun mapToDto(from: GroupPostEntity): GroupPostModel {
        return GroupPostModel(
                from.id,
                groupInPostMapper.mapToDto(from.groupInPost),
                from.postText,
                from.date,
                from.photo,
                from.commentsCount,
                from.activeCommentsCount,
                from.isActive,
                from.isOffered,
                from.images?.map { mapToDto(it) },
                from.audios?.map { mapToDto(it) },
                from.videos?.map { mapToDto(it) }
        )
    }

    fun mapToDomainEntity(from: GroupPostModel): GroupPostEntity {
        return GroupPostEntity(
                from.id,
                groupInPostMapper.mapToDomainEntity(from.groupInPost),
                from.postText,
                from.date,
                from.photo,
                from.commentsCount,
                from.activeCommentsCount,
                from.isActive,
                from.isOffered,
                from.images?.map { mapToDomainEntity(it) },
                from.audios?.map { mapToDomainEntity(it) },
                from.videos?.map { mapToDomainEntity(it) }
        )
    }

    fun mapToDto(from: FileEntity): ImageVideoModel {
        return ImageVideoModel(from.id, from.file, from.description, from.title, from.post, from.owner)
    }

    fun mapToDomainEntity(from: ImageVideoModel): FileEntity {
        return FileEntity(from.id, from.file, from.description, from.title, from.post, from.owner)
    }

    fun mapToDto(from: AudioEntity): AudioModel {
        return AudioModel(from.id, from.file, from.description, from.song, from.artist, from.genre, from.post, from.owner)
    }

    fun mapToDomainEntity(from: AudioModel): AudioEntity {
        return AudioEntity(from.id, from.file, from.description, from.song, from.artist, from.genre, from.post, from.owner)
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
                imageUrl = from.imageUrl
        )
    }

    fun mapToDomainEntity(from: CreateGroupPostModel): CreateGroupPostEntity =
            CreateGroupPostEntity(
                    postText = from.postText,
                    imageUrl = from.imageUrl)

    fun mapListToDomainEntity(from: List<GroupPostModel>): List<GroupPostEntity> =
            from.map { mapToDomainEntity(it) }
}
