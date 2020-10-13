package com.intergroupapplication.data.repository

import com.intergroupapplication.data.mapper.CommentMapper
import com.intergroupapplication.data.model.CreateCommentModel
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.domain.entity.CommentEntity
import com.intergroupapplication.domain.entity.CreateCommentEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.exception.NoMorePage
import com.intergroupapplication.domain.gateway.CommentGateway
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


    override fun createComment(postId: String, createCommentEntity: CreateCommentEntity): Single<CommentEntity> =
            api.createComment(postId, commentMapper.mapToDto(createCommentEntity))
                    .map { commentMapper.mapToDomainEntity(it) }


    override fun createAnswerToComment(answerToCommentId: String, createCommentEntity: CreateCommentEntity): Single<CommentEntity> =
            api.createAnswerToComment(answerToCommentId, commentMapper.mapToDto(createCommentEntity))
                    .map { commentMapper.mapToDomainEntity(it) }

}
