package com.intergroupapplication.domain.gateway

import com.intergroupapplication.domain.entity.CreateGroupPostEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import io.reactivex.Single

/**
 * Created by abakarmagomedov on 29/08/2018 at project InterGroupApplication.
 */
interface GroupPostGateway {
    fun getGroupPosts(groupId: String, page: Int): Single<List<GroupPostEntity>>
    fun createPost(createGroupPostEntity: CreateGroupPostEntity, groupId: String): Single<GroupPostEntity>
    fun getPostById(postId: String): Single<GroupPostEntity>
    fun getNewsPosts(page: Int): Single<List<GroupPostEntity>>
}