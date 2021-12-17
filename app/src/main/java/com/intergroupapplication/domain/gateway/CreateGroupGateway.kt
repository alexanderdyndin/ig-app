package com.intergroupapplication.domain.gateway

import com.intergroupapplication.domain.entity.CreateGroupEntity
import com.intergroupapplication.domain.entity.GroupEntity
import io.reactivex.Single

/**
 * Created by abakarmagomedov on 28/08/2018 at project InterGroupApplication.
 */
interface CreateGroupGateway {
    fun createGroup(createGroupEntity: CreateGroupEntity): Single<GroupEntity.Group>
}
