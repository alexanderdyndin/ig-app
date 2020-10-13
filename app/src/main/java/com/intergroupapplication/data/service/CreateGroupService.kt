package com.intergroupapplication.data.service

import com.intergroupapplication.data.mapper.CreateGroupMapper
import com.intergroupapplication.data.mapper.GroupMapper
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.domain.entity.CreateGroupEntity
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.domain.gateway.CreateGroupGateway
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 28/08/2018 at project InterGroupApplication.
 */
class CreateGroupService @Inject constructor(private val api: AppApi,
                                             private val createGroupMapper: CreateGroupMapper,
                                             private val groupMapper: GroupMapper) : CreateGroupGateway {


    override fun createGroup(createGroupEntity: CreateGroupEntity): Single<GroupEntity> {
        return api.createGroup(createGroupMapper.mapToDto(createGroupEntity))
                .map { groupMapper.mapToDomainEntity(it) }
    }
}