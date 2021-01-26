package com.intergroupapplication.data.remotedatasource

import androidx.paging.rxjava2.RxPagingSource
import com.intergroupapplication.data.mapper.GroupPostMapper
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.domain.entity.GroupPostEntity
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class NewsRemoteRXDataSource (private val appApi: AppApi, private val mapper: GroupPostMapper): RxPagingSource<Int, GroupPostEntity>() {

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, GroupPostEntity>> {
        return appApi.getNews(params.key?: 1)
                .subscribeOn(Schedulers.io())
                .map { mapper.mapNewsListToDomainEntity(it) }
                .map <LoadResult<Int, GroupPostEntity>> {
                    LoadResult.Page(it.news, it.previous, it.next)
                }
                .onErrorReturn { e ->
                    LoadResult.Error(e)
                }
    }

}