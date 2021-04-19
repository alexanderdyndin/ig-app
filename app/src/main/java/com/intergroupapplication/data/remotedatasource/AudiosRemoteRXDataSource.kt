package com.intergroupapplication.data.remotedatasource

import androidx.paging.PagingState
import androidx.paging.rxjava2.RxPagingSource
import com.intergroupapplication.data.mapper.GroupPostMapper
import com.intergroupapplication.data.mapper.MediaMapper
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.domain.entity.AudioEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class AudiosRemoteRXDataSource (private val appApi: AppApi,
                                private val mapper: MediaMapper): RxPagingSource<Int, AudioEntity>() {

    var key: Int = 1

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, AudioEntity>> {
        key = params.key?: 1
        return appApi.getAudios(params.key?: 1)
                .subscribeOn(Schedulers.io())
                .map { mapper.mapToDomainEntity(it) }
                .map <LoadResult<Int, AudioEntity>> {
                    LoadResult.Page(it.audios,
                            if (it.previous != null) key - 1 else null,
                            if (it.next != null) key + 1 else null)
                }
                .onErrorReturn { e ->
                    LoadResult.Error(e)
                }
    }

    override fun getRefreshKey(state: PagingState<Int, AudioEntity>): Int? {
        return null
    }

}