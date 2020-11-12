package com.intergroupapplication.data.repository

import com.intergroupapplication.data.mapper.GroupPostMapper
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.domain.FakeData
import com.intergroupapplication.domain.entity.CreateGroupPostEntity
import com.intergroupapplication.domain.entity.GroupInPostEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.entity.NewsEntity
import com.intergroupapplication.domain.exception.NoMorePage
import com.intergroupapplication.domain.gateway.GroupPostGateway
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import java.lang.Exception
import java.text.SimpleDateFormat
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

    override fun getNewsPosts(page: Int): Single<NewsEntity> {
         return api.getNews(page).map { groupPostMapper.mapNewsListToDomainEntity(it) }
                .onErrorResumeNext {
                    if (it is NoMorePage) {
                        Single.fromCallable {
                            NewsEntity(0,null,null, mutableListOf<GroupPostEntity>())
                        }
                    } else {
                        Single.error(it)
                    }
                }
    }
}
