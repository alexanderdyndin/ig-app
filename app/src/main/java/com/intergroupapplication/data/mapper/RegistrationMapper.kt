package com.intergroupapplication.data.mapper

import com.intergroupapplication.data.model.RegistrationModel
import com.intergroupapplication.data.model.RegistrationResponseModel
import com.intergroupapplication.domain.entity.RegistrationEntity
import com.intergroupapplication.domain.entity.RegistrationResponseEntity
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 03/08/2018 at project InterGroupApplication.
 */
class RegistrationMapper @Inject constructor() {

    fun mapToDataModel(from: RegistrationEntity) =
            RegistrationModel(
                    email = from.email,
                    password = from.password,
                    emailConfirm = from.emailConfirm,
                    passwordConfirm = from.passwordConfirm)


    fun mapToDomainEntity(from: RegistrationModel) =
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
