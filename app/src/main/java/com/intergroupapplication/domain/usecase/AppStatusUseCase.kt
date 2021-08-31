package com.intergroupapplication.domain.usecase

import com.intergroupapplication.data.model.ApiErrorDto
import com.intergroupapplication.data.repository.AppStatusRepository
import com.intergroupapplication.domain.gateway.AgreementsGateway
import com.intergroupapplication.domain.gateway.AppStatusGateway
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.Response
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