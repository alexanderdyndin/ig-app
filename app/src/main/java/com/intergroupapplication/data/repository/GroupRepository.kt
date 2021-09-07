package com.intergroupapplication.data.repository

import android.annotation.SuppressLint
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.rxjava2.flowable
import com.intergroupapplication.data.mappers.BansGroupMapper
import com.intergroupapplication.data.mappers.FollowersGroupMapper
import com.intergroupapplication.data.mappers.group.GroupMapper
import com.intergroupapplication.data.model.groupfollowers.GroupBanDto
import com.intergroupapplication.data.model.groupfollowers.UpdateGroupAdminDto
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.network.PAGE_SIZE
import com.intergroupapplication.data.network.dto.FollowGroupDto
import com.intergroupapplication.data.network.dto.UpdateAvatarDto
import com.intergroupapplication.data.remotedatasource.GroupBansRemoteRXDataSource
import com.intergroupapplication.data.remotedatasource.GroupFollowersRemoteRXDataSource
import com.intergroupapplication.data.remotedatasource.GroupsRemoteRXDataSource
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.domain.entity.GroupFollowEntity
import com.intergroupapplication.domain.entity.GroupUserEntity
import com.intergroupapplication.domain.exception.CanNotUploadPhoto
import com.intergroupapplication.domain.gateway.GroupGateway
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 29/08/2018 at project InterGroupApplication.
 */
class GroupRepository @Inject constructor(
    private val api: AppApi,
    private val groupMapper: GroupMapper,
    private val followersGroupMapper: FollowersGroupMapper,
    private val bansGroupMapper: BansGroupMapper
) : GroupGateway {

    @SuppressLint("CheckResult")
    override fun changeGroupAvatar(groupId: String, avatar: String): Single<GroupEntity.Group> =
        api.changeGroupAvatar(groupId, UpdateAvatarDto(avatar))
            .map { groupMapper.mapToDomainEntity(it) }
            .doOnError {
                if (it is HttpException) {
                    Completable.error(CanNotUploadPhoto())
                } else {
                    Completable.error(it)
                }
            }

    @SuppressLint("CheckResult")
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
        return api.followGroup(FollowGroupDto(groupId))
    }

    override fun unfollowGroup(groupId: String): Completable {
        return api.unfollowGroup(groupId)
    }

    @ExperimentalCoroutinesApi
    override fun getGroupList(searchFilter: String): Flowable<PagingData<GroupEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = 20,
                prefetchDistance = 1
            ),
            pagingSourceFactory = { GroupsRemoteRXDataSource(api, groupMapper, searchFilter) }
        ).flowable
    }

    @ExperimentalCoroutinesApi
    override fun getAdminGroupList(searchFilter: String): Flowable<PagingData<GroupEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = 20,
                prefetchDistance = 1
            ),
            pagingSourceFactory = {
                val ds = GroupsRemoteRXDataSource(api, groupMapper, searchFilter)
                ds.applyAdminGroupList()
                ds
            }
        ).flowable
    }

    @ExperimentalCoroutinesApi
    override fun getSubscribedGroupList(searchFilter: String): Flowable<PagingData<GroupEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = 20,
                prefetchDistance = 1
            ),
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

    @ExperimentalCoroutinesApi
    override fun getFollowers(
        groupId: String,
        searchFilter: String
    ): Flowable<PagingData<GroupUserEntity>> {
        return Pager(
            config = defaultPagingConfig,
            pagingSourceFactory = {
                GroupFollowersRemoteRXDataSource(api, followersGroupMapper, groupId, searchFilter)
            }
        ).flowable
    }

    @ExperimentalCoroutinesApi
    override fun getAdministrators(
        groupId: String,
        searchFilter: String
    ): Flowable<PagingData<GroupUserEntity>> {
        return Pager(
            config = defaultPagingConfig,
            pagingSourceFactory = {
                val followersRemote = GroupFollowersRemoteRXDataSource(
                    api,
                    followersGroupMapper,
                    groupId,
                    searchFilter
                )
                followersRemote.applyAdministratorsList()
                followersRemote
            }
        ).flowable
    }

    @ExperimentalCoroutinesApi
    override fun getBans(
        groupId: String,
        searchFilter: String
    ): Flowable<PagingData<GroupUserEntity>> {
        return Pager(
            config = defaultPagingConfig,
            pagingSourceFactory = {
                GroupBansRemoteRXDataSource(api, bansGroupMapper, groupId, searchFilter)
            }
        ).flowable
    }

    override fun banUserInGroup(userId: String, reason: String, groupId: String): Completable {
        return api.banUserInGroup(groupId, GroupBanDto(reason, userId))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun deleteUserFromBansGroup(userId: String): Completable {
        return api.deleteUserFromBansGroup(userId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun updateGroupAdmin(
        subscriptionId: String,
        updateGroupAdminDto: UpdateGroupAdminDto
    ): Completable {
        return api.updateGroupAdmin(subscriptionId, updateGroupAdminDto)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getAllGroupAdmins(groupId: String): Single<List<String>> {
        return api.getGroupFollowers(groupId, 1, "admins", "")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { groupUserFollowersDto ->
                groupUserFollowersDto.results.map {
                    it.user.id.toString()
                }
            }
    }

    override fun getGroupFollowersForSearch(
        groupId: String,
        searchFilter: String
    ): Single<List<GroupUserEntity>> {
        return api.getGroupFollowers(
            groupId = groupId,
            page = 1,
            search = searchFilter
        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                followersGroupMapper.mapToListDomainEntity(it)
            }
    }
}
