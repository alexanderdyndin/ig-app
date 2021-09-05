package com.intergroupapplication.data.service

import com.intergroupapplication.data.mappers.RegistrationMapper
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.domain.entity.EmailEntity
import com.intergroupapplication.domain.entity.RegistrationEntity
import com.intergroupapplication.domain.entity.TokenEntity
import com.intergroupapplication.domain.gateway.RegistrationGateway
import io.reactivex.Completable
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 03/08/2018 at project InterGroupApplication.
 */
class RegistrationService @Inject constructor(private val api: AppApi,
                                              private val registrationMapper: RegistrationMapper,
                                              private val userSession: UserSession) : RegistrationGateway {

    override fun performRegistration(registrationEntity: RegistrationEntity):
            Completable = api.registerUser(registrationMapper.mapToDataModel(registrationEntity))
            .flatMapCompletable {
                userSession.token = TokenEntity(it.refresh, it.access)
                userSession.email = EmailEntity(it.email)
                Completable.complete()
            }

}