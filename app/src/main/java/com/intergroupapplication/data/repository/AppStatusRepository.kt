package com.intergroupapplication.data.repository

import com.intergroupapplication.data.model.ApiErrorDto
import com.intergroupapplication.data.model.VersionModel
import com.intergroupapplication.data.network.AppApi
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.Response
import javax.inject.Inject

class AppStatusRepository @Inject constructor(private val api:AppApi) {
    fun getAppStatus(version:String): Single<String> = api.getAppStatus(VersionModel(version))
}