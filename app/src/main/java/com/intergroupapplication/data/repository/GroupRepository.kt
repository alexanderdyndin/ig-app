package com.intergroupapplication.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.rxjava2.flowable
import com.intergroupapplication.data.mapper.FollowersGroupMapper
import com.intergroupapplication.data.mapper.GroupMapper
import com.intergroupapplication.data.model.FollowGroupModel
import com.intergroupapplication.data.model.UpdateAvatarModel
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.remotedatasource.GroupFollowersRemoteRXDataSource
import com.intergroupapplication.data.network.PAGE_SIZE
import com.intergroupapplication.data.remotedatasource.GroupsRemoteRXDataSource
import com.intergroupapplication.domain.entity.*
import com.intergroupapplication.domain.exception.CanNotUploadPhoto
import com.intergroupapplication.domain.exception.NoMorePage
import com.intergroupapplication.domain.gateway.GroupGateway
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 29/08/2018 at project InterGroupApplication.
 */
class GroupRepository @Inject constructor(
        private val api: AppApi,
        private val groupMapper: GroupMapper,
        private val followersGroupMapper: FollowersGroupMapper) : GroupGateway {

    override fun changeGroupAvatar(groupId: String, avatar: String): Single<GroupEntity.Group> =
            api.changeGroupAvatar(groupId, UpdateAvatarModel(avatar))
                    .map { groupMapper.mapToDomainEntity(it) }
                    .doOnError {
                        if (it is HttpException) {
                            Completable.error(CanNotUploadPhoto())
                        } else {
                            Completable.error(it)
                        }
                    }

    override fun followersGroup(groupId: String): Single<GroupFollowEntity> {
        return api.followersGroup(groupId)
                .map { groupMapper.followsToEntity(it) }
                .doOnError {
                    Single.error<Throwable>(it)
                }
    }


    override fun getGroupDetailInfo(groupId: String): Single<GroupEntity.Group> {
        return api.getGroupInformation(groupId).map { groupMapper.mapToDomainEntity(it) }
    }

    override fun followGroup(groupId: String): Completable {
        return api.followGroup(FollowGroupModel(groupId))
    }

    override fun unfollowGroup(groupId: String): Completable {
        return api.unfollowGroup(groupId)
    }

    override fun getGroupList(searchFilter: String): Flowable<PagingData<GroupEntity>> {
        return Pager(
                config = PagingConfig(
                        pageSize = PAGE_SIZE,
                        initialLoadSize = 20,
                        prefetchDistance = 1),
                pagingSourceFactory = { GroupsRemoteRXDataSource(api, groupMapper, searchFilter) }
        ).flowable
    }

    override fun getAdminGroupList(searchFilter: String): Flowable<PagingData<GroupEntity>> {
        return Pager(
                config = PagingConfig(
                        pageSize = PAGE_SIZE,
                        initialLoadSize = 20,
                        prefetchDistance = 1),
                pagingSourceFactory = {
                    val ds = GroupsRemoteRXDataSource(api, groupMapper, searchFilter)
                    ds.applyAdminGroupList()
                    ds
                }
        ).flowable
    }

    override fun getSubscribedGroupList(searchFilter: String): Flowable<PagingData<GroupEntity>> {
        return Pager(
                config = PagingConfig(
                        pageSize = PAGE_SIZE,
                        initialLoadSize = 20,
                        prefetchDistance = 1),
                pagingSourceFactory = {
                    val ds = GroupsRemoteRXDataSource(api, groupMapper, searchFilter)
                    ds.applySubscribedGroupList()
                    ds
                }
        ).flowable
    }

    private val defaultPagingConfig = PagingConfig(
            pageSize = 20,
            initialLoadSize = 20,
            prefetchDistance = 1
    )

    override fun getFollowers(groupId: String): Flowable<PagingData<GroupUserEntity>> {
        return Pager(
                config = defaultPagingConfig,
                pagingSourceFactory = {
                    GroupFollowersRemoteRXDataSource(api, followersGroupMapper, groupId, false)
                }
        ).flowable
    }

    override fun getAdministrators(groupId: String): Flowable<PagingData<GroupUserEntity>> {
        return Pager(
                config = defaultPagingConfig,
                pagingSourceFactory = {
                    val followersRemote = GroupFollowersRemoteRXDataSource(api, followersGroupMapper, groupId, true)
                    followersRemote.applyAdministratorsList()
                    followersRemote
                }
        ).flowable
    }
}