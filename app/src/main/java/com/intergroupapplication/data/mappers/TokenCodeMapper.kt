package com.intergroupapplication.data.mappers

import com.intergroupapplication.data.network.dto.TokenDto
import com.intergroupapplication.domain.entity.TokenCodeEntity
import javax.inject.Inject

class TokenCodeMapper @Inject constructor() : (TokenDto) -> TokenCodeEntity {

    override fun invoke(tokenDto: TokenDto): TokenCodeEntity {
        return TokenCodeEntity(tokenDto.token)
    }
}
