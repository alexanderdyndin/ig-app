package com.intergroupapplication.domain.gateway

import io.reactivex.Completable

interface PermissionAuthorizeGetaway {

    fun isBlocked(): Completable

}