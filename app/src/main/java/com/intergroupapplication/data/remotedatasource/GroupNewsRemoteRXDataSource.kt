package com.intergroupapplication.data.remotedatasource

import androidx.paging.PagingState
import androidx.paging.rxjava2.RxPagingSource
import com.intergroupapplication.data.mappers.group.GroupPostMapper
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.domain.entity.GroupPostEntity
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class GroupNewsRemoteRXDataSource (private val appApi: AppApi,
                                   private val mapper: GroupPostMapper,
                                   private val groupId: String
                                   ): RxPagingSource<Int, GroupPostEntity>() {

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, GroupPostEntity>> {
        val key = params.key?: 1
        return appApi.getGroupPosts(groupId, key)
                .subscribeOn(Schedulers.io())
                .map { mapper.mapToDomainEntity(it) }
                .map <LoadResult<Int, GroupPostEntity>> {
                    LoadResult.Page(it.results,
                            if (it.previous != null) key - 1 else null,
                            if (it.next != null) key + 1 else null)
                }
                .onErrorReturn { e ->
                    LoadResult.Error(e)
                }
    }

    override fun getRefreshKey(state: PagingState<Int, GroupPostEntity>): Int? {
        //return if (state.anchorPosition != null) state.anchorPosition!! / 20 + 1 else null
        return null
    }

}