package com.intergroupapplication.domain.gateway

import com.intergroupapplication.domain.entity.AdEntity
import io.reactivex.Single

interface AppStatusGateway {
    fun getAdParameters(): Single<AdEntity>
    fun getAppStatus(version:String): Single<String>
}