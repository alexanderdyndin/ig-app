package com.intergroupapplication.domain.usecase

import androidx.paging.PagingData
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.domain.entity.GroupUserEntity
import com.intergroupapplication.domain.entity.UserEntity
import com.intergroupapplication.domain.entity.UserRole
import com.intergroupapplication.domain.gateway.GroupGateway
import com.intergroupapplication.domain.gateway.UserProfileGateway
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 31/08/2018 at project InterGroupApplication.
 */
class GroupUseCase @Inject constructor(
        private val userProfileGateway: UserProfileGateway,
        private val groupGateway: GroupGateway) {

    fun getUserRole(groupEntity: GroupEntity.Group): Single<UserRole> {
        return userProfileGateway.getUserProfile()
                .map {
                    if (it.id == groupEntity.owner) {
                        return@map UserRole.ADMIN
                    }
                    when (groupEntity.isFollowing) {
                        true -> UserRole.USER_FOLLOWER
                        false -> UserRole.USER_NOT_FOLLOWER
                    }

                }
    }


    fun getGroupList(searchFilter:String): Flowable<PagingData<GroupEntity>> =
            groupGateway.getGroupList(searchFilter)

    fun getSubscribedGroupList(searchFilter:String): Flowable<PagingData<GroupEntity>> =
            groupGateway.getSubscribedGroupList(searchFilter)

    fun getAdminGroupList(searchFilter:String): Flowable<PagingData<GroupEntity>> =
            groupGateway.getAdminGroupList(searchFilter)

    fun subscribeGroup(groupId: String): Completable {
        return groupGateway.followGroup(groupId)
    }

    fun unsubscribeGroup(groupId: String): Completable {
        return groupGateway.unfollowGroup(groupId)
    }

    fun getGroupFollowers(groupId: String): Flowable<PagingData<GroupUserEntity>> =
            groupGateway.getFollowers(groupId)

    fun getGroupAdministrators(groupId: String): Flowable<PagingData<GroupUserEntity>> =
            groupGateway.getAdministrators(groupId)

    fun getGroupBans(groupId: String): Flowable<PagingData<GroupUserEntity>> =
            groupGateway.getBans(groupId)

    fun banUserInGroup(userId: String, reason: String, groupId: String): Completable =
            groupGateway.banUserInGroup(userId, reason, groupId)

}