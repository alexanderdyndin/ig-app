package com.intergroupapplication.data.repository

import com.intergroupapplication.data.mapper.UserProfileMapper
import com.intergroupapplication.data.model.VersionModel
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.domain.entity.AdEntity
import com.intergroupapplication.domain.gateway.AppStatusGateway
import io.reactivex.Single
import javax.inject.Inject

class AppStatusRepository @Inject constructor(private val api:AppApi,
                                              private val userProfileMapper: UserProfileMapper
                                              ): AppStatusGateway {
    override fun getAdParameters(): Single<AdEntity> {
        return api.adCountInfo()
                .map { userProfileMapper.mapToDomainEntity(it) }
//                .doOnSuccess {
//                    sessionStorage.countAd = it
//                }
//                .doOnError { Completable.error(it) }
    }

    override fun getAppStatus(version:String): Single<String> =
            api.getAppStatus(VersionModel(version))
}