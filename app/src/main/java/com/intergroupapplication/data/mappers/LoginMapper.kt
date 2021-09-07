package com.intergroupapplication.data.mappers

import com.intergroupapplication.data.model.SocialAuthModel
import com.intergroupapplication.data.network.dto.LoginDto
import com.intergroupapplication.domain.entity.LoginEntity
import com.intergroupapplication.domain.entity.SocialAuthEntity
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 03/08/2018 at project InterGroupApplication.
 */
class LoginMapper @Inject constructor() {

    fun mapToDomainEntity(from: LoginDto) =
        LoginEntity(
            email = from.email,
            password = from.password
        )

    fun mapToDataModel(from: LoginEntity) =
        LoginDto(
            email = from.email,
            password = from.password
        )

    fun mapToDomainEntity(from: SocialAuthModel) =
        SocialAuthEntity(
            authToken = from.authToken
        )

    fun mapToDataModel(from: SocialAuthEntity) =
        SocialAuthModel(
            authToken = from.authToken
        )
}
