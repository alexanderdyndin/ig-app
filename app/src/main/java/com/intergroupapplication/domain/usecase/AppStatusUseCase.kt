package com.intergroupapplication.domain.usecase

import com.intergroupapplication.data.model.ApiErrorDto
import com.intergroupapplication.data.repository.AppStatusRepository
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.Response
import javax.inject.Inject

class AppStatusUseCase @Inject constructor(private val appStatusRepository: AppStatusRepository) {
    operator fun invoke(version: String): Single<String> {
        return appStatusRepository.getAppStatus(version)
    }

    fun getAdParameters() = appStatusRepository.getAdParameters()
}