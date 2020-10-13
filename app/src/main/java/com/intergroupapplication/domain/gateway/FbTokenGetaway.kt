package com.intergroupapplication.domain.gateway

import com.intergroupapplication.data.model.DeviceModel
import io.reactivex.Completable

interface FbTokenGetaway {

    fun refreshToken(deviceModel: DeviceModel, idUser: String): Completable

}