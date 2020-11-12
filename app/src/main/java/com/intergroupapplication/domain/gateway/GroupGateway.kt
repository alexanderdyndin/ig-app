package com.intergroupapplication.domain.gateway

import com.intergroupapplication.data.model.CommentsDto
import com.intergroupapplication.data.model.GroupModel
import com.intergroupapplication.data.model.GroupsDto
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.domain.entity.GroupListEntity
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Created by abakarmagomedov on 29/08/2018 at project InterGroupApplication.
 */
interface GroupGateway {
    fun getGroupList(page: Int, searchFilter:String): Single<GroupListEntity>
    fun getSubscribedGroupList(page: Int, searchFilter:String): Single<GroupListEntity>
    fun getAdminGroupList(page: Int, searchFilter:String): Single<GroupListEntity>
    fun getGroupDetailInfo(groupId: String): Single<GroupEntity>
    fun followGroup(groupId: String): Completable
    fun unfollowGroup(groupId: String): Completable
    fun changeGroupAvatar(groupId: String, avatar: String): Single<GroupEntity>
}
