package com.intergroupapplication.domain.gateway

import androidx.paging.PagingData
import com.intergroupapplication.domain.entity.CommentEntity
import com.intergroupapplication.domain.entity.CreateCommentEntity
import com.intergroupapplication.domain.entity.ReactsEntity
import com.intergroupapplication.domain.entity.ReactsEntityRequest
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Created by abakarmagomedov on 03/09/2018 at project InterGroupApplication.
 */
interface CommentGateway {
    fun getComments(postId: String, page: String): Flowable<PagingData<CommentEntity>>
    fun createComment(
        postId: String,
        createCommentEntity: CreateCommentEntity
    ): Single<CommentEntity.Comment>

    fun createAnswerToComment(
        answerToCommentId: String,
        createCommentEntity: CreateCommentEntity
    ): Single<CommentEntity.Comment>

    fun editComment(
        commentId: String,
        createCommentEntity: CreateCommentEntity
    ): Single<CommentEntity.Comment>

    fun deleteComment(commentId: String): Completable
    fun setCommentReact(
        commentId: String,
        reactsEntityRequest: ReactsEntityRequest
    ): Single<ReactsEntity>
}
