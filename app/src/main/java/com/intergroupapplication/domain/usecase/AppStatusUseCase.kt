package com.intergroupapplication.domain.usecase

import com.intergroupapplication.domain.gateway.AgreementsGateway
import com.intergroupapplication.domain.gateway.AppStatusGateway
import javax.inject.Inject

class AppStatusUseCase @Inject constructor(
    private val appStatusRepository: AppStatusGateway,
    private val agreementsGateway: AgreementsGateway
    ) {
    fun getAppStatus(version: String) = appStatusRepository.getAppStatus(version)

    fun getAdParameters() = appStatusRepository.getAdParameters()

    fun getTerms() = agreementsGateway.getTerms()

    fun getPrivacy() = agreementsGateway.getPrivacy()

    fun getRightHolders() = agreementsGateway.getRightHolders()

}