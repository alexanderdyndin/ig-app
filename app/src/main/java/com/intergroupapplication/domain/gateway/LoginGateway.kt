package com.intergroupapplication.domain.gateway

import com.intergroupapplication.domain.entity.LoginEntity
import com.intergroupapplication.domain.entity.TokenEntity
import io.reactivex.Single

/**
 * Created by abakarmagomedov on 03/08/2018 at project InterGroupApplication.
 */
interface LoginGateway {
    fun performLogin(loginEntity: LoginEntity): Single<TokenEntity>
}
