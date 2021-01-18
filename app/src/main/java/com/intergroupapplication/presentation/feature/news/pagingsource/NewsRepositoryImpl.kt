package com.intergroupapplication.presentation.feature.news.pagingsource

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.rxjava2.flowable
import com.intergroupapplication.domain.entity.GroupPostEntity
import io.reactivex.Flowable
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(private val pagingSource: NewsDataSource3): NewsRepository {

    override fun getNews(): Flowable<PagingData<GroupPostEntity>> {
        return Pager(
                config = PagingConfig(
                        pageSize = 20,
                        enablePlaceholders = false,
                        prefetchDistance = 5,
                        initialLoadSize = 40),
                pagingSourceFactory = { pagingSource }
        ).flowable
    }

}
