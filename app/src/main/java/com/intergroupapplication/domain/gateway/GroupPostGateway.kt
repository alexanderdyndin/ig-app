package com.intergroupapplication.domain.gateway

import androidx.paging.PagingData
import com.intergroupapplication.domain.entity.CreateGroupPostEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.entity.NewsEntity
import io.reactivex.Flowable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow

/**
 * Created by abakarmagomedov on 29/08/2018 at project InterGroupApplication.
 */
interface GroupPostGateway {
    fun getGroupPosts(groupId: String, page: Int): Single<List<GroupPostEntity>>
    fun createPost(createGroupPostEntity: CreateGroupPostEntity, groupId: String): Single<GroupPostEntity>
    fun getPostById(postId: String): Single<GroupPostEntity>
    fun getNewsPosts(): Flowable<PagingData<GroupPostEntity>>
    fun editPost(createGroupPostEntity: CreateGroupPostEntity, postId: String): Single<GroupPostEntity>
}