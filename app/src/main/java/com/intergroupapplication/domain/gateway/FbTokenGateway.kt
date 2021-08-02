package com.intergroupapplication.domain.gateway

import com.intergroupapplication.data.model.DeviceModel
import io.reactivex.Completable

interface FbTokenGateway {

    fun refreshToken(deviceModel: DeviceModel, idUser: String): Completable

}