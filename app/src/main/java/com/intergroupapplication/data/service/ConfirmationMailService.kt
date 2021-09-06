package com.intergroupapplication.data.service

import com.intergroupapplication.data.mappers.TokenMapper
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.network.dto.TokenConfirmDto
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.domain.gateway.ConfirmationMailGateway
import io.reactivex.Completable
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 03/08/2018 at project InterGroupApplication.
 */
class ConfirmationMailService @Inject constructor(
    private val api: AppApi,
    private val tokenMapper: TokenMapper,
    private val sessionStorage: UserSession
) : ConfirmationMailGateway {

    override fun confirmMail(confirmCode: String): Completable =
        api.confirmMail(TokenConfirmDto(confirmCode))
            .map { sessionStorage.token = tokenMapper.mapToDomainEntity(it) }
            .ignoreElement()
}
