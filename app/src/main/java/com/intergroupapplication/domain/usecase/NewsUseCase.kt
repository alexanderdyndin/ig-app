package com.intergroupapplication.domain.usecase

import androidx.paging.PagingData
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.gateway.GroupPostGateway
import io.reactivex.Flowable
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NewsUseCase @Inject constructor(private val groupPostGateway: GroupPostGateway) {

    fun getNewsPosts(): Flowable<PagingData<GroupPostEntity>> {
        return groupPostGateway.getNewsPosts()
    }
}