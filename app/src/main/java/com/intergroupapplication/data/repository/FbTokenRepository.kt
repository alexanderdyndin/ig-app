package com.intergroupapplication.data.repository

import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.network.dto.DeviceDto
import com.intergroupapplication.domain.gateway.FbTokenGateway
import javax.inject.Inject

class FbTokenRepository @Inject constructor(private val api: AppApi) : FbTokenGateway {
    override fun refreshToken(deviceDto: DeviceDto, idUser: String) =
        api.updateToken(deviceDto)
}
