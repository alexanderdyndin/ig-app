package com.intergroupapplication.data.mapper

import com.intergroupapplication.data.model.CreateGroupPostModel
import com.intergroupapplication.data.model.GroupPostModel
import com.intergroupapplication.data.model.NewsDto
import com.intergroupapplication.domain.entity.CreateGroupPostEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.entity.NewsEntity
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 29/08/2018 at project InterGroupApplication.
 */
class GroupPostMapper @Inject constructor(val groupInPostMapper: GroupInPostMapper) {

    fun mapToDto(from: GroupPostEntity): GroupPostModel {
        return GroupPostModel(
                id = from.id,
                postText = from.postText,
                commentsCount = from.commentsCount,
                date = from.date,
                groupInPost = groupInPostMapper.mapToDto(from.groupInPost),
                photo = from.photo
        )
    }

    fun mapToDomainEntity(from: GroupPostModel): GroupPostEntity {
        return GroupPostEntity(
                id = from.id,
                postText = from.postText,
                commentsCount = from.commentsCount ?: "0",
                date = from.date,
                groupInPost = groupInPostMapper.mapToDomainEntity(from.groupInPost),
                photo = from.photo
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
