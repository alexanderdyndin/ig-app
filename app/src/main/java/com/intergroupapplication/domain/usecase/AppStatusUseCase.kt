package com.intergroupapplication.domain.usecase

import com.intergroupapplication.data.repository.AppStatusRepository
import javax.inject.Inject

class AppStatusUseCase @Inject constructor(private val appStatusRepository: AppStatusRepository) {
    fun getAppStatus(version: String) = appStatusRepository.getAppStatus(version)

    fun getAdParameters() = appStatusRepository.getAdParameters()
}