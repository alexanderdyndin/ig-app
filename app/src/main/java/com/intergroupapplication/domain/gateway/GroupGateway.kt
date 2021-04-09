package com.intergroupapplication.domain.gateway

import androidx.paging.PagingData
import com.intergroupapplication.data.model.CommentsDto
import com.intergroupapplication.data.model.GroupModel
import com.intergroupapplication.data.model.GroupsDto
import com.intergroupapplication.domain.entity.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Created by abakarmagomedov on 29/08/2018 at project InterGroupApplication.
 */
interface GroupGateway {
    fun getGroupList(searchFilter:String): Flowable<PagingData<GroupEntity>>
    fun getSubscribedGroupList(searchFilter:String): Flowable<PagingData<GroupEntity>>
    fun getAdminGroupList(searchFilter:String): Flowable<PagingData<GroupEntity>>
    fun getGroupDetailInfo(groupId: String): Single<GroupEntity>
    fun followGroup(groupId: String): Completable
    fun unfollowGroup(groupId: String): Completable
    fun changeGroupAvatar(groupId: String, avatar: String): Single<GroupEntity>
    fun followersGroup(groupId: String): Single<GroupFollowEntity>
    fun getFollowers(groupId: String): Flowable<PagingData<GroupUserEntity>>
    fun getAdministrators(groupId: String): Flowable<PagingData<GroupUserEntity>>
    fun getBans(groupId: String): Flowable<PagingData<GroupUserEntity>>
    fun banUserInGroup(userId: String, reason: String, groupId: String): Completable
}
