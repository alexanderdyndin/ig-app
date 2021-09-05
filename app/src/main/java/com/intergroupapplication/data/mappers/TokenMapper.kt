package com.intergroupapplication.data.mappers

import com.intergroupapplication.data.model.TokenModel
import com.intergroupapplication.domain.entity.TokenEntity
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 03/08/2018 at project InterGroupApplication.
 */
class TokenMapper @Inject constructor() {

    fun mapToDomainEntity(from: TokenModel) =
        TokenEntity(
            refresh = from.refresh,
            access = from.access
        )


    fun mapToDataModel(from: TokenEntity) =
        TokenModel(
            refresh = from.refresh,
            access = from.access
        )
}
