package com.intergroupapplication.domain.gateway

import io.reactivex.Completable

interface ResendCodeGateway {

    fun resendCode(): Completable

}