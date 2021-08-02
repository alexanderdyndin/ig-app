package com.intergroupapplication.data.repository

import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.domain.gateway.PermissionAuthorizeGetaway
import javax.inject.Inject

class PermissionAuthorizeRepository @Inject constructor(private val api: AppApi) :
    PermissionAuthorizeGetaway {
    override fun isBlocked() = api.isBlocked()
}