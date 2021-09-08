package com.intergroupapplication.data.mappers

import com.intergroupapplication.data.network.dto.CreateGroupDto
import com.intergroupapplication.domain.entity.CreateGroupEntity
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 28/08/2018 at project InterGroupApplication.
 */
class CreateGroupMapper @Inject constructor() {

    fun mapToDto(from: CreateGroupEntity): CreateGroupDto {
        return CreateGroupDto(
            name = from.name,
            description = from.description,
            avatar = from.avatar,
            subject = from.subject,
            rules = from.rules,
            isClosed = from.isClosed,
            ageRestriction = from.ageRestriction
        )
    }

    fun mapToDomainEntity(from: CreateGroupDto): CreateGroupEntity {
        return CreateGroupEntity(
            name = from.name,
            description = from.description,
            avatar = from.avatar,
            subject = from.subject,
            rules = from.rules,
            isClosed = from.isClosed,
            ageRestriction = from.ageRestriction
        )
    }
}
