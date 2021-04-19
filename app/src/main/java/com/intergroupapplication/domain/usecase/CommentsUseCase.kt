package com.intergroupapplication.domain.usecase

import com.intergroupapplication.domain.entity.CreateCommentEntity
import com.intergroupapplication.domain.entity.ReactsEntityRequest
import com.intergroupapplication.domain.gateway.CommentGateway
import com.intergroupapplication.domain.gateway.ComplaintsGateway
import javax.inject.Inject

class CommentsUseCase @Inject constructor(private val complaintsGateway: ComplaintsGateway,
                                          private val commentGateway: CommentGateway) {

    fun complaintComment(commentId: Int) = complaintsGateway.complaintComment(commentId)

    fun createComment(postId: String, comment: String) =
            commentGateway.createComment(postId, CreateCommentEntity(comment))

    fun getComments(postId: String, page: String) = commentGateway.getComments(postId, page)

    fun createAnswer(commentId: String, comment: String) =
            commentGateway.createAnswerToComment(commentId, CreateCommentEntity(comment))

    fun deleteComment(commentId: Int) = commentGateway.deleteComment(commentId.toString())

    fun setReact(isLike: Boolean, isDislike: Boolean, commentId: Int) =
            commentGateway.setCommentReact(commentId.toString(), ReactsEntityRequest(isLike, isDislike))
}