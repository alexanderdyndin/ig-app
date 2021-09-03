package com.intergroupapplication.data.mapper

import com.intergroupapplication.data.db.entity.NewsPostDb
import com.intergroupapplication.data.model.NewsModel
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.entity.GroupPostsEntity
import com.intergroupapplication.domain.entity.NewsEntity
import javax.inject.Inject

class NewsModelToNewsPostDbMapper @Inject constructor() : (NewsModel) -> NewsPostDb {
    override fun invoke(newsModel: NewsModel): NewsPostDb {
        return NewsPostDb(
            id = newsModel.id,
            postId = newsModel.post.id,
            groupId = newsModel.post.groupId,
            activeCommentsCount = newsModel.post.activeCommentsCount,
            groupInPost = newsModel.post.groupInPost,
            bells = newsModel.post.bells,
            reacts = newsModel.post.reacts,
            images = newsModel.post.images,
            audios = newsModel.post.audios,
            videos = newsModel.post.videos,
            photo = newsModel.post.photo,
            postText = newsModel.post.postText,
            date = newsModel.post.date,
            updated = newsModel.post.updated,
            commentsCount = newsModel.post.commentsCount,
            isActive = newsModel.post.isActive,
            isOffered = newsModel.post.isOffered,
            idp = newsModel.post.idp,
            isPinned = newsModel.post.isPinned,
            pin = newsModel.post.pin,
            author = newsModel.post.author,
            unreadComments = newsModel.post.unreadComments,
            user = newsModel.user
        )
    }
}

class NewsPostDbToNewsEntity @Inject constructor(
    private val groupPostMapper: GroupPostMapper,
    private val groupInPostMapper: GroupInPostMapper,
    private val userProfileMapper: UserProfileMapper,
    private val mediaMapper: MediaMapper,
    private val reactsMapper: ReactsMapper
) : (NewsPostDb) -> NewsEntity {
    override fun invoke(newsPostDb: NewsPostDb): NewsEntity {
        return NewsEntity.Post(
            id = newsPostDb.id,
            post = GroupPostEntity.PostEntity(
                id = newsPostDb.postId,
                bells = groupPostMapper.mapToDomainEntity(newsPostDb.bells),
                groupInPost = groupInPostMapper.mapToDomainEntity(newsPostDb.groupInPost),
                postText = newsPostDb.postText,
                updated = newsPostDb.updated,
                author = userProfileMapper.mapToDomainEntity(newsPostDb.author),
                pin = newsPostDb.pin,
                photo = newsPostDb.photo,
                commentsCount = newsPostDb.commentsCount,
                activeCommentsCount = newsPostDb.activeCommentsCount,
                isActive = newsPostDb.isActive,
                isOffered = newsPostDb.isOffered,
                isPinned = newsPostDb.isPinned,
                reacts = reactsMapper.mapToDomainEntity(newsPostDb.reacts),
                idp = newsPostDb.idp,
                images = newsPostDb.images.map { mediaMapper.mapToDomainEntity(it) },
                videos = newsPostDb.videos.map { mediaMapper.mapToDomainEntity(it) },
                audios = newsPostDb.audios.map { mediaMapper.mapToDomainEntity(it) },
                unreadComments = newsPostDb.unreadComments,
                date = newsPostDb.date
            ),
            user = newsPostDb.user
        )
    }

}