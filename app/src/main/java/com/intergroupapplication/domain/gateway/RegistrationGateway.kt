package com.intergroupapplication.domain.gateway

import com.intergroupapplication.domain.entity.RegistrationEntity
import io.reactivex.Completable

/**
 * Created by abakarmagomedov on 03/08/2018 at project InterGroupApplication.
 */
interface RegistrationGateway {
    fun performRegistration(registrationEntity: RegistrationEntity): Completable
}
