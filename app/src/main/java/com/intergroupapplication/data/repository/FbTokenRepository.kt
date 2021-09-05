package com.intergroupapplication.data.repository

import com.intergroupapplication.data.model.DeviceModel
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.domain.gateway.FbTokenGateway
import javax.inject.Inject

class FbTokenRepository @Inject constructor(private val api: AppApi) : FbTokenGateway {
    override fun refreshToken(deviceModel: DeviceModel, idUser: String) =
        api.updateToken(deviceModel)
}
