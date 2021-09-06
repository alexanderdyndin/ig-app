package com.intergroupapplication.data.mappers

import com.intergroupapplication.data.network.dto.TokenAccessDto
import com.intergroupapplication.domain.entity.TokenEntity
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 03/08/2018 at project InterGroupApplication.
 */
class TokenMapper @Inject constructor() {

    fun mapToDomainEntity(from: TokenAccessDto) =
        TokenEntity(
            refresh = from.refresh,
            access = from.access
        )


    fun mapToDataModel(from: TokenEntity) =
        TokenAccessDto(
            refresh = from.refresh,
            access = from.access
        )
}
