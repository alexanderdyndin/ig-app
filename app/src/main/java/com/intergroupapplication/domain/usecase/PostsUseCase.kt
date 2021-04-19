package com.intergroupapplication.domain.usecase

import com.intergroupapplication.domain.entity.*
import com.intergroupapplication.domain.gateway.ComplaintsGateway
import com.intergroupapplication.domain.gateway.GroupPostGateway
import io.reactivex.Single
import javax.inject.Inject

class PostsUseCase @Inject constructor(private val groupPostGateway: GroupPostGateway,
                                        private val complaintsGateway: ComplaintsGateway) {

    fun getNews() = groupPostGateway.getNewsPosts()

    fun getGroupPosts(groupId: String) = groupPostGateway.getGroupPosts(groupId)

    fun createPost(createGroupPostEntity: CreateGroupPostEntity, groupId: String) =
            groupPostGateway.createPost(createGroupPostEntity, groupId)

    fun editPost(groupPostEntity: GroupPostEntity.PostEntity): Single<GroupPostEntity.PostEntity> {
        val createGroupPostEntity = CreateGroupPostEntity(groupPostEntity.postText,
                groupPostEntity.images.map { FileRequestEntity(it.file, it.description, it.title) },
                groupPostEntity.audios.map {
                    AudioRequestEntity(it.file, it.description, it.song, it.artist, it.genre)
                },
                groupPostEntity.videos.map { FileRequestEntity(it.file, it.description, it.title) },
                groupPostEntity.isPinned, groupPostEntity.pin)
        return groupPostGateway.editPost(createGroupPostEntity, groupPostEntity.id)
    }

    fun editPost(createGroupPostEntity:CreateGroupPostEntity, postId: String
    ): Single<GroupPostEntity.PostEntity> {
        return groupPostGateway.editPost(createGroupPostEntity, postId)
    }

    fun getPost(postId: String) =
            groupPostGateway.getPostById(postId)

    fun setReact(isLike: Boolean, isDislike: Boolean, postId: String) =
            groupPostGateway.setReact(ReactsEntityRequest(isLike, isDislike), postId)

    fun sendComplaint(postId: Int) = complaintsGateway.complaintPost(postId)

    fun deleteNewsPost(postId: Int) = groupPostGateway.deleteNewsPost(postId.toString())

    fun deleteGroupPost(postId: Int) = groupPostGateway.deleteGroupPost(postId.toString())

    fun getBell(postId: String) = groupPostGateway.getPostBell(postId)

    fun deleteBell(postId: String) = groupPostGateway.deleteBell(postId)

    fun setBell(postId: String) =
            groupPostGateway.setPostBell(BellFollowEntity(null, null, postId.toInt()))
}