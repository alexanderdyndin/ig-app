package com.intergroupapplication.data.service

import com.intergroupapplication.data.mappers.LoginMapper
import com.intergroupapplication.data.mappers.TokenMapper
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.domain.entity.LoginEntity
import com.intergroupapplication.domain.entity.TokenEntity
import com.intergroupapplication.domain.gateway.LoginGateway
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 03/08/2018 at project InterGroupApplication.
 */
class LoginService @Inject constructor(
    private val api: AppApi,
    private val loginMapper: LoginMapper,
    private val tokenMapper: TokenMapper,
    private val sessionStorage: UserSession
) : LoginGateway {

    override fun performLogin(loginEntity: LoginEntity): Single<TokenEntity> =
        api.loginUser(loginMapper.mapToDataModel(loginEntity))
            .map {
                val tokenEntity = tokenMapper.mapToDomainEntity(it)
                sessionStorage.token = tokenEntity
                tokenEntity
            }

}
