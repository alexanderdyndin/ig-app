package com.intergroupapplication.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.rxjava2.flowable
import com.intergroupapplication.data.mapper.GroupPostMapper
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.remotedatasource.NewsRemoteRXDataSource
import com.intergroupapplication.domain.entity.CreateGroupPostEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.entity.NewsEntity
import com.intergroupapplication.domain.exception.NoMorePage
import com.intergroupapplication.domain.gateway.GroupPostGateway
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.*
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 29/08/2018 at project InterGroupApplication.
 */
class GroupPostsRepository @Inject constructor(private val api: AppApi,
                                               private val groupPostMapper: GroupPostMapper)
    : GroupPostGateway {

    override fun getPostById(postId: String): Single<GroupPostEntity> {
        return api.getPostById(postId).map { groupPostMapper.mapToDomainEntity(it) }
    }

    override fun getGroupPosts(groupId: String, page: Int): Single<List<GroupPostEntity>> {
        return api.getGroupPosts(groupId, page).map { groupPostMapper.mapListToDomainEntity(it.results) }
                .onErrorResumeNext {
                    if (it is NoMorePage) {
                        Single.fromCallable { mutableListOf<GroupPostEntity>() }
                    } else {
                        Single.error(it)
                    }
                }
    }

    override fun createPost(createGroupPostEntity: CreateGroupPostEntity,
                            groupId: String): Single<GroupPostEntity> {
        return api.createPost(groupPostMapper.mapToDto(createGroupPostEntity), groupId)
                .map { groupPostMapper.mapToDomainEntity(it) }
    }

    override fun getNewsPosts(): Flowable<PagingData<GroupPostEntity>> {
        return Pager(
                config = PagingConfig(
                        pageSize = 20,
                        enablePlaceholders = false,
                        maxSize = 30,
                        prefetchDistance = 5,
                        initialLoadSize = 40),
                pagingSourceFactory = { NewsRemoteRXDataSource(api, groupPostMapper) }
        ).flowable
    }

}
