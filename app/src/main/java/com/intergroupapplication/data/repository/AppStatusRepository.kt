package com.intergroupapplication.data.repository

import com.intergroupapplication.data.mappers.UserProfileMapper
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.network.dto.VersionDto
import com.intergroupapplication.domain.entity.AdEntity
import com.intergroupapplication.domain.gateway.AppStatusGateway
import io.reactivex.Single
import javax.inject.Inject

class AppStatusRepository @Inject constructor(
    private val api: AppApi,
    private val userProfileMapper: UserProfileMapper
) : AppStatusGateway {
    override fun getAdParameters(): Single<AdEntity> {
        return api.adCountInfo()
            .map { userProfileMapper.mapToDomainEntity(it) }
    }

    override fun getAppStatus(version: String): Single<String> =
        api.getAppStatus(VersionDto(version))
}
