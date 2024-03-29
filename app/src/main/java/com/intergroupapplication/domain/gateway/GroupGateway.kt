package com.intergroupapplication.domain.gateway

import androidx.paging.PagingData
import com.intergroupapplication.data.model.groupfollowers.UpdateGroupAdminDto
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.domain.entity.GroupFollowEntity
import com.intergroupapplication.domain.entity.GroupUserEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Created by abakarmagomedov on 29/08/2018 at project InterGroupApplication.
 */
interface GroupGateway {
    fun getGroupList(searchFilter: String): Flowable<PagingData<GroupEntity>>
    fun getSubscribedGroupList(searchFilter: String): Flowable<PagingData<GroupEntity>>
    fun getAdminGroupList(searchFilter: String): Flowable<PagingData<GroupEntity>>
    fun getGroupDetailInfo(groupId: String): Single<GroupEntity.Group>
    fun followGroup(groupId: String): Completable
    fun unfollowGroup(groupId: String): Completable
    fun changeGroupAvatar(groupId: String, avatar: String): Single<GroupEntity.Group>
    fun followersGroup(groupId: String): Single<GroupFollowEntity>
    fun getFollowers(groupId: String, searchFilter: String): Flowable<PagingData<GroupUserEntity>>
    fun getAdministrators(
        groupId: String,
        searchFilter: String
    ): Flowable<PagingData<GroupUserEntity>>

    fun getBans(groupId: String, searchFilter: String): Flowable<PagingData<GroupUserEntity>>
    fun banUserInGroup(userId: String, reason: String, groupId: String): Completable
    fun deleteUserFromBansGroup(userId: String): Completable
    fun updateGroupAdmin(
        subscriptionId: String,
        updateGroupAdminDto: UpdateGroupAdminDto
    ): Completable

    fun getAllGroupAdmins(groupId: String): Single<List<String>>
    fun getGroupFollowersForSearch(
        groupId: String,
        searchFilter: String
    ): Single<List<GroupUserEntity>>
}
