package com.intergroupapplication.data.repository

import android.content.Context
import androidx.paging.*
import androidx.paging.rxjava2.flowable
import com.intergroupapplication.data.db.IgDatabase
import com.intergroupapplication.data.mapper.GroupPostMapper
import com.intergroupapplication.data.mapper.ReactsMapper
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.network.PAGE_SIZE
import com.intergroupapplication.data.remote_mediator.GroupPostMediatorRXDataSource
import com.intergroupapplication.data.remotedatasource.NewsRemoteRXDataSource
import com.intergroupapplication.domain.entity.*
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
                                               private val reactsMapper: ReactsMapper,
                                               context: Context
                                               ): GroupPostGateway {

    // todo
    private val db = IgDatabase.getInstance(context)

    override fun getPostById(postId: String): Single<GroupPostEntity.PostEntity> {
        return api.getPostById(postId).map { groupPostMapper.mapToDomainEntity(it) }
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

    @ExperimentalPagingApi
    override fun getGroupPosts(groupId: String): Flowable<PagingData<GroupPostEntity>> {
        return Pager(
            config = PagingConfig(PAGE_SIZE, 5),
            remoteMediator = GroupPostMediatorRXDataSource(api, db, groupPostMapper, groupId)
        ) {
            db.groupPost().getAllGroupPostsModel(groupId)
        }.flowable.map { paging ->
            paging.map { groupPostModel ->
                groupPostMapper.mapToDomainEntity(groupPostModel)
            }
        }
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
