package com.intergroupapplication.data.mapper.group

import com.intergroupapplication.data.model.GroupInPostModel
import com.intergroupapplication.domain.entity.GroupInPostEntity
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 06/09/2018 at project InterGroupApplication.
 */
class GroupInPostMapper @Inject constructor() {

    fun mapToDto(from: GroupInPostEntity): GroupInPostModel =
            GroupInPostModel(
                    id = from.id,
                    name = from.name,
                    avatar = from.avatar
            )

    fun mapToDomainEntity(from: GroupInPostModel): GroupInPostEntity =
            GroupInPostEntity(
                    id = from.id,
                    name = from.name,
                    avatar = from.avatar
            )

}