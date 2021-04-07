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
class CommentMapper @Inject constructor(private val userProfileMapper: UserProfileMapper,
                                        private val reactsMapper: ReactsMapper,
                                        private val mediaMapper: MediaMapper
) {

    fun mapToDomainEntity(from: CommentModel): CommentEntity.Comment =
            CommentEntity.Comment(
                    id = from.id,
                    commentOwner = userProfileMapper.mapToDomainEntity(from.commentOwner),
                    reacts = reactsMapper.mapToDomainEntity(from.reacts),
                    images = from.images.map { mediaMapper.mapToDomainEntity(it) },
                    audios = from.audios.map { mediaMapper.mapToDomainEntity(it) },
                    videos = from.videos.map { mediaMapper.mapToDomainEntity(it) },
                    text = from.text,
                    date = from.date,
                    isActive = from.isActive,
                    idc = from.idc,
                    post = from.post,
                    answerTo = if (from.answerTo != null) mapToDomainEntity(from.answerTo) else null
            )

    fun mapToDto(from: CommentEntity.Comment): CommentModel =
            CommentModel(
                    id = from.id,
                    commentOwner = if (from.commentOwner != null)
                        userProfileMapper.mapToDataModel(from.commentOwner) else null,
                    reacts = reactsMapper.mapToDto(from.reacts),
                    images = from.images.map { mediaMapper.mapToDto(it) },
                    audios = from.audios.map { mediaMapper.mapToDto(it) },
                    videos = from.videos.map { mediaMapper.mapToDto(it) },
                    text = from.text,
                    date = from.date,
                    isActive = from.isActive,
                    idc = from.idc,
                    post = from.post,
                    answerTo = if (from.answerTo != null) mapToDto(from.answerTo) else null
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
