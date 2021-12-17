package com.intergroupapplication.data.remotedatasource

import androidx.paging.PagingState
import androidx.paging.rxjava2.RxPagingSource
import com.intergroupapplication.data.mappers.MediaMapper
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.domain.entity.FileEntity
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class ImagesRemoteRXDataSource(
    private val appApi: AppApi,
    private val mapper: MediaMapper
) : RxPagingSource<Int, FileEntity>() {

    var key: Int = 1

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, FileEntity>> {
        key = params.key ?: 1
        return appApi.getImages(params.key ?: 1)
            .subscribeOn(Schedulers.io())
            .map { mapper.mapToDomainEntity(it) }
            .map<LoadResult<Int, FileEntity>> {
                LoadResult.Page(
                    it.images,
                    if (it.previous != null) key - 1 else null,
                    if (it.next != null) key + 1 else null
                )
            }
            .onErrorReturn { e ->
                LoadResult.Error(e)
            }
    }

    override fun getRefreshKey(state: PagingState<Int, FileEntity>): Int? {
        return null
    }
}
