package com.intergroupapplication.data.mappers

import com.intergroupapplication.data.model.CreateGroupModel
import com.intergroupapplication.domain.entity.CreateGroupEntity
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 28/08/2018 at project InterGroupApplication.
 */
class CreateGroupMapper @Inject constructor() {

    fun mapToDto(from: CreateGroupEntity): CreateGroupModel {
        return CreateGroupModel(
            name = from.name,
            description = from.description,
            avatar = from.avatar,
            subject = from.subject,
            rules = from.rules,
            isClosed = from.isClosed,
            ageRestriction = from.ageRestriction
        )
    }

    fun mapToDomainEntity(from: CreateGroupModel): CreateGroupEntity {
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
