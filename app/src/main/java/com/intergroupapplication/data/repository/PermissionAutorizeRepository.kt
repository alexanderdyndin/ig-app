package com.intergroupapplication.data.repository

import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.domain.gateway.PermissionAutorizeGetaway
import javax.inject.Inject

class PermissionAutorizeRepository @Inject constructor(private val api: AppApi) : PermissionAutorizeGetaway {
    override fun isBlocked() = api.isBlocked()
}