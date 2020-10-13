package com.intergroupapplication.data.mapper

import com.intergroupapplication.data.model.LoginModel
import com.intergroupapplication.domain.entity.LoginEntity
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 03/08/2018 at project InterGroupApplication.
 */
class LoginMapper @Inject constructor() {

    fun mapToDomainEntity(from: LoginModel) =
            LoginEntity(
                    email = from.email,
                    password = from.password
            )

    fun mapToDataModel(from: LoginEntity) =
            LoginModel(
                    email = from.email,
                    password = from.password
            )

}
