package com.intergroupapplication.domain.usecase

import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.domain.entity.UserRole
import com.intergroupapplication.domain.gateway.UserProfileGateway
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 31/08/2018 at project InterGroupApplication.
 */
class GroupUseCase @Inject constructor(
        private val userProfileGateway: UserProfileGateway) {

    fun getUserRole(groupEntity: GroupEntity): Single<UserRole> {
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
}