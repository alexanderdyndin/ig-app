package com.intergroupapplication.domain.gateway

import com.intergroupapplication.domain.entity.CreateUserEntity
import com.intergroupapplication.domain.entity.UserEntity
import io.reactivex.Single

/**
 * Created by abakarmagomedov on 06/08/2018 at project InterGroupApplication.
 */
interface UserProfileGateway {
    fun createUserProfile(createUserEntity: CreateUserEntity): Single<UserEntity>
    fun getUserProfile(): Single<UserEntity>
    fun changeUserProfileAvatar(avatar: String): Single<String>
    fun setEmail(email: String)
}
