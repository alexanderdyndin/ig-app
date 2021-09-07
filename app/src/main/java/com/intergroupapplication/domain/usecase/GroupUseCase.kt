package com.intergroupapplication.domain.usecase

import android.annotation.SuppressLint
import androidx.paging.PagingData
import com.intergroupapplication.data.model.AddBlackListUserModel
import com.intergroupapplication.data.model.groupfollowers.UpdateGroupAdminDto
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.domain.entity.GroupUserEntity
import com.intergroupapplication.domain.entity.UserRole
import com.intergroupapplication.domain.gateway.GroupGateway
import com.intergroupapplication.domain.gateway.UserProfileGateway
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 31/08/2018 at project InterGroupApplication.
 */
class GroupUseCase @Inject constructor(
    private val userProfileGateway: UserProfileGateway,
    private val groupGateway: GroupGateway
) {

    @SuppressLint("CheckResult")
    fun getUserRole(groupEntity: GroupEntity.Group): Single<UserRole> {
        if (groupEntity.isFollowing) {
            return groupGateway.getAllGroupAdmins(groupEntity.id)
                .zipWith(userProfileGateway.getUserProfile(), { admins, user ->
                    admins.forEach {
                        if (user.id == it) return@zipWith UserRole.ADMIN
                    }
                    UserRole.USER_FOLLOWER
                })
                .onErrorReturnItem(UserRole.USER_FOLLOWER)
        } else {
            return Single.just(UserRole.USER_NOT_FOLLOWER)
        }
    }

    fun getGroupList(searchFilter: String): Flowable<PagingData<GroupEntity>> =
        groupGateway.getGroupList(searchFilter)

    fun getSubscribedGroupList(searchFilter: String): Flowable<PagingData<GroupEntity>> =
        groupGateway.getSubscribedGroupList(searchFilter)

    fun getAdminGroupList(searchFilter: String): Flowable<PagingData<GroupEntity>> =
        groupGateway.getAdminGroupList(searchFilter)

    fun subscribeGroup(groupId: String): Completable {
        return groupGateway.followGroup(groupId)
    }

    fun unsubscribeGroup(groupId: String): Completable {
        return groupGateway.unfollowGroup(groupId)
    }

    fun getGroupFollowers(
        groupId: String,
        searchFilter: String
    ): Flowable<PagingData<GroupUserEntity>> =
        groupGateway.getFollowers(groupId, searchFilter)

    fun getGroupAdministrators(
        groupId: String,
        searchFilter: String
    ): Flowable<PagingData<GroupUserEntity>> =
        groupGateway.getAdministrators(groupId, searchFilter)

    fun getGroupBans(groupId: String, searchFilter: String): Flowable<PagingData<GroupUserEntity>> =
        groupGateway.getBans(groupId, searchFilter)

    fun banUserInGroup(userId: String, reason: String, groupId: String): Completable =
        groupGateway.banUserInGroup(userId, reason, groupId)

    fun deleteUserFromBansGroup(userId: String) =
        groupGateway.deleteUserFromBansGroup(userId)

    fun updateGroupAdmin(subscriptionId: String, toAdmin: Boolean): Completable {
        val updateGroupAdmin = UpdateGroupAdminDto(
            isAdmin = toAdmin
        )
        return groupGateway.updateGroupAdmin(subscriptionId, updateGroupAdmin)
    }

    fun getGroupFollowersForSearch(
        groupId: String,
        searchFilter: String
    ): Single<List<AddBlackListUserModel>> {
        return groupGateway.getGroupFollowersForSearch(groupId, searchFilter)
            .map { listUsers ->
                listUsers.map { groupUserEntity ->
                    AddBlackListUserModel(
                        fullName = "${groupUserEntity.firstName} ${groupUserEntity.surName}",
                        avatar = groupUserEntity.avatar,
                        idProfile = groupUserEntity.idProfile,
                        isAdministrator = groupUserEntity.isAdministrator,
                        isOwner = groupUserEntity.isOwner,
                        isBlocked = groupUserEntity.isBlocked,
                        subscriptionId = groupUserEntity.subscriptionId
                    )
                }
            }
            .zipWith(userProfileGateway.getUserProfile(), { followers, currentUser ->
                followers.filter {
                    it.idProfile != currentUser.id && !it.isOwner
                }
            })
    }

    fun getCurrentUserId() =
        userProfileGateway.getUserProfile().map {
            it.id
        }
}
