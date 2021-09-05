package com.intergroupapplication.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.rxjava2.flowable
import com.intergroupapplication.data.mappers.CommentMapper
import com.intergroupapplication.data.mappers.ReactsMapper
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.network.PAGE_SIZE
import com.intergroupapplication.data.remotedatasource.CommentsRemoteRXDataSource
import com.intergroupapplication.domain.entity.CommentEntity
import com.intergroupapplication.domain.entity.CreateCommentEntity
import com.intergroupapplication.domain.entity.ReactsEntity
import com.intergroupapplication.domain.entity.ReactsEntityRequest
import com.intergroupapplication.domain.gateway.CommentGateway
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 03/09/2018 at project InterGroupApplication.
 */
class CommentRepository @Inject constructor(
    private val api: AppApi,
    private val commentMapper: CommentMapper,
    private val reactsMapper: ReactsMapper
) : CommentGateway {

    @ExperimentalCoroutinesApi
    override fun getComments(postId: String, page: String): Flowable<PagingData<CommentEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = 5
            ),
            pagingSourceFactory = {
                CommentsRemoteRXDataSource(
                    api,
                    commentMapper,
                    postId,
                    page.toInt()
                )
            }
        ).flowable
    }

    override fun createComment(
        postId: String,
        createCommentEntity: CreateCommentEntity
    ): Single<CommentEntity.Comment> =
        api.createComment(postId, commentMapper.mapToDto(createCommentEntity))
            .map {
                commentMapper.mapToDomainEntity(it)
            }

    override fun createAnswerToComment(
        answerToCommentId: String,
        createCommentEntity: CreateCommentEntity
    ): Single<CommentEntity.Comment> =
        api.createAnswerToComment(answerToCommentId, commentMapper.mapToDto(createCommentEntity))
            .map { commentMapper.mapToDomainEntity(it) }

    override fun deleteComment(commentId: String): Completable {
        return api.deleteComment(commentId)
    }

    override fun setCommentReact(
        commentId: String,
        reactsEntityRequest: ReactsEntityRequest
    ): Single<ReactsEntity> {
        return api.setCommentReact(reactsMapper.mapToDto(reactsEntityRequest), commentId)
            .map { reactsMapper.mapToDomainEntity(it) }
    }
}
