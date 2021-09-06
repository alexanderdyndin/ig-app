package com.intergroupapplication.data.mappers

import com.intergroupapplication.data.network.dto.LoginDto
import com.intergroupapplication.domain.entity.LoginEntity
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
}
