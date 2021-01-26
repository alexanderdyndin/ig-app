package com.intergroupapplication.data.repository

import com.intergroupapplication.data.mapper.GroupMapper
import com.intergroupapplication.data.model.FollowGroupModel
import com.intergroupapplication.data.model.GroupModel
import com.intergroupapplication.data.model.UpdateAvatarModel
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.domain.entity.GroupFollowEntity
import com.intergroupapplication.domain.entity.GroupListEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.exception.CanNotUploadPhoto
import com.intergroupapplication.domain.exception.NoMorePage
import com.intergroupapplication.domain.gateway.GroupGateway
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 29/08/2018 at project InterGroupApplication.
 */
class GroupRepository @Inject constructor(private val api: AppApi,
                                          private val groupMapper: GroupMapper) : GroupGateway {

    override fun getGroupList(page: Int, searchFilter:String): Single<GroupListEntity> {
        return api.getGroupList(page, searchFilter)
                .map { groupMapper.mapToDomainEntity(it) }
                .onErrorResumeNext {
                    if (it is NoMorePage) {
                        Single.fromCallable { GroupListEntity(0, null, null, mutableListOf<GroupEntity>()) }
                    } else {
                        Single.error(it)
                    }
                }
    }

    override fun getAdminGroupList(page: Int, searchFilter:String): Single<GroupListEntity> {
        return api.getAdminGroupList(page, searchFilter)
                .map { groupMapper.mapToDomainEntity(it) }
                .onErrorResumeNext {
                    if (it is NoMorePage) {
                        Single.fromCallable { GroupListEntity(0, null, null, mutableListOf<GroupEntity>()) }
                    } else {
                        Single.error(it)
                    }
                }
    }

    override fun getSubscribedGroupList(page: Int, searchFilter: String): Single<GroupListEntity> {
        return api.getSubscribedGroupList(page, searchFilter)
                .map { groupMapper.mapToDomainEntity(it) }
                .onErrorResumeNext {
                    if (it is NoMorePage) {
                        Single.fromCallable { GroupListEntity(0, null, null, mutableListOf<GroupEntity>()) }
                    } else {
                        Single.error(it)
                    }
                }
    }

    override fun changeGroupAvatar(groupId: String, avatar: String): Single<GroupEntity> =
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

    override fun getGroupDetailInfo(groupId: String): Single<GroupEntity> {
        return api.getGroupInformation(groupId).map { groupMapper.mapToDomainEntity(it) }
    }

    override fun followGroup(groupId: String): Completable {
        return api.followGroup(FollowGroupModel(groupId))
    }

    override fun unfollowGroup(groupId: String): Completable {
        return api.unfollowGroup(groupId)
    }
}