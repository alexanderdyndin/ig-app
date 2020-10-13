package com.intergroupapplication.domain.gateway

import io.reactivex.Completable

/**
 * Created by abakarmagomedov on 03/08/2018 at project InterGroupApplication.
 */
interface ConfirmationMailGateway {
    fun confirmMail(confirmCode: String): Completable
}
