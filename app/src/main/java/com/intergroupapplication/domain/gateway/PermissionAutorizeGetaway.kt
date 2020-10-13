package com.intergroupapplication.domain.gateway

import io.reactivex.Completable

interface PermissionAutorizeGetaway {

    fun isBlocked(): Completable

}