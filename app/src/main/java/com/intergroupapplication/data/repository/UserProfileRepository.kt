package com.intergroupapplication.data.repository

import com.intergroupapplication.data.mapper.UserProfileMapper
import com.intergroupapplication.data.model.UpdateAvatarModel
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.domain.entity.AdEntity
import com.intergroupapplication.domain.entity.CreateUserEntity
import com.intergroupapplication.domain.entity.UserEntity
import com.intergroupapplication.domain.exception.CanNotUploadPhoto
import com.intergroupapplication.domain.gateway.UserProfileGateway
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 06/08/2018 at project InterGroupApplication.
 */
class UserProfileRepository @Inject constructor(private val api: AppApi,
                                                private val userProfileMapper: UserProfileMapper,
                                                private val sessionStorage: UserSession) : UserProfileGateway {

    override fun createUserProfile(createUserEntity: CreateUserEntity): Single<UserEntity> =
            api.createUserProfile(userProfileMapper.mapToDataModel(createUserEntity))
                    .map {
                        val userProfile = userProfileMapper.mapToDomainEntity(it)
                        sessionStorage.user = userProfile
                        userProfile
                    }

    override fun changeUserProfileAvatar(avatar: String): Single<String> =
            getUserProfile()
                    .flatMap { api.changeUserAvatar(it.id, UpdateAvatarModel(avatar)) }
                    .map { it.avatar }
                    .doOnError {
                        if (it is HttpException) {
                            Completable.error(CanNotUploadPhoto())
                        } else {
                            Completable.error(it)
                        }
                    }
                    .doOnSuccess {
                        val oldUser = sessionStorage.user
                        oldUser?.let { userEntity ->
                            val newUser = userEntity.copy(avatar = it)
                            sessionStorage.user = newUser
                        }
                    }

    override fun getAdParameters(): Single<AdEntity> {
        return api.adCountInfo()
                .map { userProfileMapper.mapToDomainEntity(it) }
                .doOnSuccess {
                    sessionStorage.countAd = it
                }
                .doOnError { Completable.error(it) }
    }

    override fun getUserProfile(): Single<UserEntity> {
        return if (sessionStorage.user != null) {
            Single.fromCallable { sessionStorage.user }
        } else {
            uploadUserProfile()
        }
    }

    private fun uploadUserProfile(): Single<UserEntity> {
        return api.getUserProfile()
                .map {
                    val userProfile = userProfileMapper.mapToDomainEntity(it)
                    sessionStorage.user = userProfile
                    userProfile
                }
    }

}
