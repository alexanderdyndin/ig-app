package com.intergroupapplication.domain.gateway

import com.intergroupapplication.data.network.dto.DeviceDto
import io.reactivex.Completable

interface FbTokenGateway {

    fun refreshToken(deviceDto: DeviceDto, idUser: String): Completable
}
