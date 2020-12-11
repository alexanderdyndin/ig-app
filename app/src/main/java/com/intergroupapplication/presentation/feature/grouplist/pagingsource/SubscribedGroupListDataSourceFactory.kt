package com.intergroupapplication.presentation.feature.grouplist.pagingsource

import androidx.paging.DataSource
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.presentation.feature.group.pagingsource.GroupPostDataSource
import javax.inject.Inject

class SubscribedGroupListDataSourceFactory @Inject constructor(val source: SubscribedGroupListDataSource) :
        DataSource.Factory<Int, GroupEntity>() {
    override fun create(): DataSource<Int, GroupEntity> = source

}