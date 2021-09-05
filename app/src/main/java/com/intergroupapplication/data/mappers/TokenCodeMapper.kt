package com.intergroupapplication.data.mappers

import com.intergroupapplication.data.network.dto.TokenDto
import com.intergroupapplication.domain.entity.TokenCodeEntity
import javax.inject.Inject

class TokenCodeMapper @Inject constructor() {

    fun map(from: TokenDto) = TokenCodeEntity(from.token)
}
