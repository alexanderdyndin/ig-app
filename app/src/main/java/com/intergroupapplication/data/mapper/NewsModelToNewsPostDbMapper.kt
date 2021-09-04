package com.intergroupapplication.data.mapper

import com.intergroupapplication.data.db.entity.NewsPostDb
import com.intergroupapplication.data.mapper.group.GroupPostDbToEntityMapper
import com.intergroupapplication.data.mapper.group.GroupPostDtoToDbMapper
import com.intergroupapplication.data.network.dto.NewsPostDto
import com.intergroupapplication.domain.entity.NewsEntity
import javax.inject.Inject

class NewsModelToNewsPostDbMapper @Inject constructor(
    private val groupPostMapper: GroupPostDtoToDbMapper
) : (NewsPostDto) -> NewsPostDb {
    override fun invoke(newsPostDto: NewsPostDto): NewsPostDb {
        return NewsPostDb(
            id = newsPostDto.id,
            post = groupPostMapper(newsPostDto.post),
            user = newsPostDto.user
        )
    }
}

class NewsPostDbToNewsEntity @Inject constructor(
    private val groupPostDbToEntityMapper: GroupPostDbToEntityMapper,
) : (NewsPostDb) -> NewsEntity {
    override fun invoke(newsPostDb: NewsPostDb): NewsEntity {
        return NewsEntity.Post(
            id = newsPostDb.id,
            post = groupPostDbToEntityMapper(newsPostDb.post),
            user = newsPostDb.user
        )
    }
}
