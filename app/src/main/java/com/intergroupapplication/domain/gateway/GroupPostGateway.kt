package com.intergroupapplication.domain.gateway

import androidx.paging.PagingData
import com.intergroupapplication.data.db.entity.GroupPostDbModel
import com.intergroupapplication.domain.entity.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Created by abakarmagomedov on 29/08/2018 at project InterGroupApplication.
 */
interface GroupPostGateway {
    fun getGroupPosts(groupId: String): Flowable<PagingData<GroupPostEntity>>
    fun createPost(createGroupPostEntity: CreateGroupPostEntity, groupId: String): Single<GroupPostEntity.PostEntity>
    fun getPostById(postId: String): Single<GroupPostEntity.PostEntity>
    fun getNewsPosts(): Flowable<PagingData<NewsEntity>>
    fun editPost(createGroupPostEntity: CreateGroupPostEntity, postId: String): Single<GroupPostEntity.PostEntity>
    fun setReact(reactsEntityRequest: ReactsEntityRequest, postId: String): Single<ReactsEntity>
    fun deleteGroupPost(postId: String): Completable
    fun deleteNewsPost(postId: String): Completable
    fun getPostBell(postId: String): Single<BellFollowEntity>
    fun setPostBell(bellFollowEntity: BellFollowEntity): Single<BellFollowEntity>
    fun deleteBell(postId: String): Completable
}