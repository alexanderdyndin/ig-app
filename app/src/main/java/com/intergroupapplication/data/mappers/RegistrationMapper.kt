package com.intergroupapplication.data.mappers

import com.intergroupapplication.data.model.RegistrationResponseModel
import com.intergroupapplication.data.network.dto.RegistrationDto
import com.intergroupapplication.domain.entity.RegistrationEntity
import com.intergroupapplication.domain.entity.RegistrationResponseEntity
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 03/08/2018 at project InterGroupApplication.
 */
class RegistrationMapper @Inject constructor() {

    fun mapToDataModel(from: RegistrationEntity) =
        RegistrationDto(
            email = from.email,
            password = from.password,
            emailConfirm = from.emailConfirm,
            passwordConfirm = from.passwordConfirm
        )


    fun mapToDomainEntity(from: RegistrationDto) =
        RegistrationEntity(
            email = from.email,
            password = from.password,
            emailConfirm = from.emailConfirm,
            passwordConfirm = from.passwordConfirm
        )


    fun mapToDomainEntity(from: RegistrationResponseModel) =
        RegistrationResponseEntity(
            isActive = from.isActive,
            email = from.email
        )


    fun mapToDomainEntity(from: RegistrationResponseEntity) =
        RegistrationResponseModel(
            isActive = from.isActive,
            email = from.email
        )
}
