package com.intergroupapplication.data.repository

import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.domain.gateway.ResendCodeGateway
import javax.inject.Inject

class ResendCodeRepository @Inject constructor(private val api: AppApi) : ResendCodeGateway {
    override fun resendCode() = api.resendCode()
}
