package com.intergroupapplication.domain.usecase

import com.intergroupapplication.domain.gateway.AppStatusGateway
import javax.inject.Inject

class AppStatusUseCase @Inject constructor(private val appStatusGateway: AppStatusGateway) {
    fun getAppStatus(version: String) = appStatusGateway.getAppStatus(version)

    fun getAdParameters() = appStatusGateway.getAdParameters()
}