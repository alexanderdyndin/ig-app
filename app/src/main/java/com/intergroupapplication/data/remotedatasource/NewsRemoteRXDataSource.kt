package com.intergroupapplication.data.remotedatasource

import androidx.paging.PagingState
import androidx.paging.rxjava2.RxPagingSource
import com.intergroupapplication.data.mapper.GroupPostMapper
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.entity.NewsEntity
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class NewsRemoteRXDataSource (private val appApi: AppApi,
                              private val mapper: GroupPostMapper): RxPagingSource<Int, NewsEntity>() {

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, NewsEntity>> {
        return appApi.getNews(params.key?: 1)
                .subscribeOn(Schedulers.io())
                .map { mapper.mapNewsListToDomainEntity(it) }
                .map <LoadResult<Int, NewsEntity>> {
                    LoadResult.Page(it.news, it.previous, it.next)
                }
                .onErrorReturn { e ->
                    LoadResult.Error(e)
                }
    }

    override fun getRefreshKey(state: PagingState<Int, NewsEntity>): Int? {
        //return if (state.anchorPosition != null) state.anchorPosition!! / 20 + 1 else null
        return null
    }

}