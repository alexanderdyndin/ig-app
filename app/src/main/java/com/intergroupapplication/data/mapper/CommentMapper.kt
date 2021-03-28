package com.intergroupapplication.data.mapper

import com.intergroupapplication.data.model.CommentModel
import com.intergroupapplication.data.model.CommentsDto
import com.intergroupapplication.data.model.CreateCommentModel
import com.intergroupapplication.domain.entity.CommentEntity
import com.intergroupapplication.domain.entity.CommentsEntity
import com.intergroupapplication.domain.entity.CreateCommentEntity
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 03/09/2018 at project InterGroupApplication.
 */
class CommentMapper @Inject constructor(private val userProfileMapper: UserProfileMapper) {

    fun mapToDomainEntity(from: CommentModel): CommentEntity.Comment =
            CommentEntity.Comment(
                    id = from.id,
                    text = from.text,
                    date = from.date,
                    commentOwner = userProfileMapper.mapToDomainEntity(from.commentOwner),
                    answerTo = from.answerTo
            )

    fun mapToDto(from: CommentEntity.Comment): CommentModel =
            CommentModel(
                    id = from.id,
                    text = from.text,
                    date = from.date,
                    commentOwner = userProfileMapper.mapToDataModel(from.commentOwner!!),
                    answerTo = from.answerTo
            )

    fun mapToDto(from: CreateCommentEntity): CreateCommentModel =
            CreateCommentModel(text = from.text)

    fun mapToDomainEntity(from: CreateCommentModel): CreateCommentEntity =
            CreateCommentEntity(text = from.text)

    fun mapListToDomainEntity(from: List<CommentModel>): List<CommentEntity> =
            from.map { mapToDomainEntity(it) }


    fun mapToDto(from: CommentsEntity): CommentsDto =
            CommentsDto(
                    from.count.toString(),
                    from.next,
                    from.previous,
                    from.comments.map { mapToDto(it) }
            )

    fun mapToDomainEntity(from: CommentsDto): CommentsEntity =
            CommentsEntity(
                    from.count.toInt(),
                    from.next,
                    from.previous,
                    from.comments.map { mapToDomainEntity(it) }
            )

}
