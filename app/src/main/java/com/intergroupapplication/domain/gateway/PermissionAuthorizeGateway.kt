package com.intergroupapplication.domain.gateway

import io.reactivex.Completable

interface PermissionAuthorizeGateway {

    fun isBlocked(): Completable
}
