package com.intergroupapplication.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.rxjava2.flowable
import com.intergroupapplication.data.mapper.CommentMapper
import com.intergroupapplication.data.model.CreateCommentModel
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.remotedatasource.CommentsRemoteRXDataSource
import com.intergroupapplication.data.remotedatasource.NewsRemoteRXDataSource
import com.intergroupapplication.domain.entity.CommentEntity
import com.intergroupapplication.domain.entity.CreateCommentEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.exception.NoMorePage
import com.intergroupapplication.domain.gateway.CommentGateway
import io.reactivex.Flowable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 03/09/2018 at project InterGroupApplication.
 */
class CommentRepository @Inject constructor(private val api: AppApi,
                                            private val commentMapper: CommentMapper)
    : CommentGateway {

    override fun getComments(postId: String, page: Int): Single<List<CommentEntity>> =
            api.getPostComments(postId, page).map { commentMapper.mapListToDomainEntity(it.comments) }
                    .onErrorResumeNext {
                        if (it is NoMorePage) {
                            Single.fromCallable { mutableListOf<CommentEntity>() }
                        } else {
                            Single.error(it)
                        }
                    }

    override fun getComments(postId: String): Flowable<PagingData<CommentEntity>> {
        return Pager(
                config = PagingConfig(
                        pageSize = 20,
                        prefetchDistance = 5),
                pagingSourceFactory = { CommentsRemoteRXDataSource(api, commentMapper, postId) }
        ).flowable
    }


    override fun createComment(postId: String, createCommentEntity: CreateCommentEntity): Single<CommentEntity> =
            api.createComment(postId, commentMapper.mapToDto(createCommentEntity))
                    .map { commentMapper.mapToDomainEntity(it) }


    override fun createAnswerToComment(answerToCommentId: String, createCommentEntity: CreateCommentEntity): Single<CommentEntity> =
            api.createAnswerToComment(answerToCommentId, commentMapper.mapToDto(createCommentEntity))
                    .map { commentMapper.mapToDomainEntity(it) }

}
