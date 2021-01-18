package com.intergroupapplication.presentation.feature.news.pagingsource

import androidx.paging.PagingData
import com.intergroupapplication.domain.entity.GroupPostEntity
import io.reactivex.Flowable

interface NewsRepository {
    fun getNews(): Flowable<PagingData<GroupPostEntity>>
}
