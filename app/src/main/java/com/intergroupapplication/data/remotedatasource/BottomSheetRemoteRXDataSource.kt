package com.intergroupapplication.data.remotedatasource

import androidx.paging.PagingState
import androidx.paging.rxjava2.RxPagingSource
import com.intergroupapplication.domain.entity.BottomSheetEntity
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber


class BottomSheetRemoteRXDataSource(private val entity:BottomSheetEntity, private var updateKey:Int?)
    :RxPagingSource<Int,String>() {
    override fun getRefreshKey(state: PagingState<Int, String>): Int?{
        return null
    }

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, String>> {
        var key =  params.key?:0
        updateKey = 1
        if (key<0) key = 0
        val size = entity.mediaList.size
        val listData:List<String>
        listData = when {
            size<=30 -> {
                entity.mediaList
            }
            key *31< size -> {
                entity.mediaList.subList(key * 30, key * 31)
            }
            else -> entity.mediaList.subList(size-31, size-1)
        }
        return Single.just(entity).subscribeOn(Schedulers.io())
                .map <LoadResult<Int, String>> {
                    LoadResult.Page(listData,
                           if (key == 0) null else key-1,
                            if (size>=key*31)key+1 else null)
                }
                .onErrorReturn { e ->
                    LoadResult.Error(e)
                }
    }

}