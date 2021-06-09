package com.intergroupapplication.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.rxjava2.flowable
import com.intergroupapplication.data.mapper.GroupPostMapper
import com.intergroupapplication.data.mapper.ReactsMapper
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.network.PAGE_SIZE
import com.intergroupapplication.data.remotedatasource.GroupNewsRemoteRXDataSource
import com.intergroupapplication.data.remotedatasource.NewsRemoteRXDataSource
import com.intergroupapplication.domain.entity.*
import com.intergroupapplication.domain.exception.NoMorePage
import com.intergroupapplication.domain.gateway.GroupPostGateway
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 29/08/2018 at project InterGroupApplication.
 */
@ExperimentalCoroutinesApi
class GroupPostsRepository @Inject constructor(private val api: AppApi,
                                               private val groupPostMapper: GroupPostMapper,
                                               private val reactsMapper: ReactsMapper
                                               ): GroupPostGateway {

    override fun getPostById(postId: String): Single<CommentEntity.PostEntity> {
        return api.getPostById(postId).map { groupPostMapper.mapToPostEntity(it) }
    }

    override fun createPost(createGroupPostEntity: CreateGroupPostEntity,
                            groupId: String): Single<GroupPostEntity.PostEntity> {
        return api.createPost(groupPostMapper.mapToDto(createGroupPostEntity), groupId)
                .map { groupPostMapper.mapToDomainEntity(it) }
    }

    override fun getNewsPosts(): Flowable<PagingData<NewsEntity>> {
        return Pager(
                config = PagingConfig(
                        pageSize = PAGE_SIZE,
                        prefetchDistance = 5),
                pagingSourceFactory = { NewsRemoteRXDataSource(api, groupPostMapper) }
        ).flowable
    }

    override fun getGroupPosts(groupId: String): Flowable<PagingData<GroupPostEntity>> {
        return Pager(
                config = PagingConfig(
                        pageSize = PAGE_SIZE,
                        prefetchDistance = 5),
                pagingSourceFactory = { GroupNewsRemoteRXDataSource(api, groupPostMapper, groupId) }
        ).flowable
    }

    override fun editPost(createGroupPostEntity: CreateGroupPostEntity, postId: String): Single<GroupPostEntity.PostEntity> {
        return api.editPostById(postId, groupPostMapper.mapToDto(createGroupPostEntity))
                .map { groupPostMapper.mapToDomainEntity(it) }
    }

    override fun setReact(reactsEntityRequest: ReactsEntityRequest, postId: String): Single<ReactsEntity> {
        return api.setReact(reactsMapper.mapToDto(reactsEntityRequest), postId)
                .map { reactsMapper.mapToDomainEntity(it) }
    }

    override fun deleteGroupPost(postId: String): Completable {
        return api.deleteGroupPost(postId)
    }

    override fun deleteNewsPost(postId: String): Completable {
        return api.deleteNewsPost(postId)
    }

    override fun getPostBell(postId: String): Single<BellFollowEntity> {
        return api.getBell(postId).map { groupPostMapper.mapToDomainEntity(it) }
    }

    override fun setPostBell(bellFollowEntity: BellFollowEntity): Single<BellFollowEntity> {
        return api.setBell(groupPostMapper.mapToDto(bellFollowEntity)).map { groupPostMapper.mapToDomainEntity(it) }
    }

    override fun deleteBell(postId: String): Completable {
        return api.deleteBell(postId)
    }

}
