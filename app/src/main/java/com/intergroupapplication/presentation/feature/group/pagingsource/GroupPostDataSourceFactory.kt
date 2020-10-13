package com.intergroupapplication.presentation.feature.group.pagingsource

import androidx.paging.DataSource
import com.intergroupapplication.domain.entity.GroupPostEntity
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 19/09/2018 at project InterGroupApplication.
 */
class GroupPostDataSourceFactory @Inject constructor(val source: GroupPostDataSource) :
        DataSource.Factory<Int, GroupPostEntity>() {
    override fun create(): DataSource<Int, GroupPostEntity> = source
}