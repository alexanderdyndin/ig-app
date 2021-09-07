package com.intergroupapplication.data.repository

import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.domain.gateway.PermissionAuthorizeGateway
import javax.inject.Inject

class PermissionAuthorizeRepository @Inject constructor(private val api: AppApi) :
    PermissionAuthorizeGateway {
    override fun isBlocked() = api.isBlocked()
}
