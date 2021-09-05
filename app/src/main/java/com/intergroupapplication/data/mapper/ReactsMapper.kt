package com.intergroupapplication.data.mapper

import com.intergroupapplication.data.model.ReactsModel
import com.intergroupapplication.data.model.ReactsModelRequest
import com.intergroupapplication.domain.entity.ReactsEntity
import com.intergroupapplication.domain.entity.ReactsEntityRequest
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 29/08/2018 at project InterGroupApplication.
 */
class ReactsMapper @Inject constructor() {

    fun mapToDto(from: ReactsEntity): ReactsModel =
            ReactsModel(
                    isLike = from.isLike,
                    isDislike = from.isDislike,
                    likesCount = from.likesCount,
                    dislikesCount = from.dislikesCount
            )

    fun mapToDomainEntity(from: ReactsModel): ReactsEntity =
            ReactsEntity(
                    isLike = from.isLike,
                    isDislike = from.isDislike,
                    likesCount = from.likesCount,
                    dislikesCount = from.dislikesCount
            )

    fun mapToDto(from: ReactsEntityRequest): ReactsModelRequest =
            ReactsModelRequest(
                    isLike = from.isLike,
                    isDislike = from.isDislike
            )

    fun mapToDomainEntity(from: ReactsModelRequest): ReactsEntityRequest =
            ReactsEntityRequest(
                    isLike = from.isLike,
                    isDislike = from.isDislike
            )
}
