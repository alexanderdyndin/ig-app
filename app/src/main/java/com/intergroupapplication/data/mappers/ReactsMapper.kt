package com.intergroupapplication.data.mappers

import com.intergroupapplication.data.db.entity.ReactsDb
import com.intergroupapplication.data.network.dto.ReactsDto
import com.intergroupapplication.data.network.dto.ReactsRequestDto
import com.intergroupapplication.domain.entity.ReactsEntity
import com.intergroupapplication.domain.entity.ReactsEntityRequest
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 29/08/2018 at project InterGroupApplication.
 */
class ReactsMapper @Inject constructor() {

    fun mapToDto(from: ReactsEntity): ReactsDto =
        ReactsDto(
            isLike = from.isLike,
            isDislike = from.isDislike,
            likesCount = from.likesCount,
            dislikesCount = from.dislikesCount
        )

    fun mapDbToEntity(from: ReactsDb): ReactsEntity =
        ReactsEntity(
            isLike = from.isLike,
            isDislike = from.isDislike,
            likesCount = from.likesCount,
            dislikesCount = from.dislikesCount
        )

    fun mapDtoToDb(from: ReactsDto): ReactsDb =
        ReactsDb(
            isLike = from.isLike,
            isDislike = from.isDislike,
            likesCount = from.likesCount,
            dislikesCount = from.dislikesCount
        )

    fun mapToDomainEntity(from: ReactsDto): ReactsEntity =
        ReactsEntity(
            isLike = from.isLike,
            isDislike = from.isDislike,
            likesCount = from.likesCount,
            dislikesCount = from.dislikesCount
        )

    fun mapToDto(from: ReactsEntityRequest): ReactsRequestDto =
        ReactsRequestDto(
            isLike = from.isLike,
            isDislike = from.isDislike
        )

    fun mapToDomainEntity(from: ReactsRequestDto): ReactsEntityRequest =
        ReactsEntityRequest(
            isLike = from.isLike,
            isDislike = from.isDislike
        )
}
