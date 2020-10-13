package com.intergroupapplication.domain.gateway

import com.intergroupapplication.data.model.CreateCommentModel
import com.intergroupapplication.domain.entity.CommentEntity
import com.intergroupapplication.domain.entity.CreateCommentEntity
import io.reactivex.Single

/**
 * Created by abakarmagomedov on 03/09/2018 at project InterGroupApplication.
 */
interface CommentGateway {
    fun getComments(postId: String, page: Int): Single<List<CommentEntity>>
    fun createComment(postId: String, createCommentEntity: CreateCommentEntity): Single<CommentEntity>
    fun createAnswerToComment(answerToCommentId: String,
                              createCommentEntity: CreateCommentEntity): Single<CommentEntity>
}