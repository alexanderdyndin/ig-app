package com.intergroupapplication.presentation.feature.news.pagingsource

import androidx.paging.DataSource
import com.intergroupapplication.domain.entity.GroupPostEntity
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 16/10/2018 at project InterGroupApplication.
 */
class NewsDataSourceFactory @Inject constructor(val source: NewsDataSource) :
        DataSource.Factory<Int, GroupPostEntity>() {
    override fun create(): DataSource<Int, GroupPostEntity> = source
}