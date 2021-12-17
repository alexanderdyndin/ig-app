package com.intergroupapplication.data.remotedatasource

import androidx.paging.PagingState
import androidx.paging.rxjava2.RxPagingSource
import com.intergroupapplication.data.mappers.BansGroupMapper
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.domain.entity.GroupUserEntity
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class GroupBansRemoteRXDataSource(
    private val appApi: AppApi,
    private val mapper: BansGroupMapper,
    private val groupId: String,
    private val searchFilter: String
) : RxPagingSource<Int, GroupUserEntity>() {

    private var key = 1

    override fun getRefreshKey(state: PagingState<Int, GroupUserEntity>): Int {
        val position = state.anchorPosition ?: 0
        return position / 20 + 1
    }

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, GroupUserEntity>> {
        key = params.key ?: 1
        return appApi.getGroupBans(groupId, key, searchFilter)
            .subscribeOn(Schedulers.io())
            .map {
                mapper.mapToDomainEntity(it)
            }
            .map<LoadResult<Int, GroupUserEntity>> {
                LoadResult.Page(
                    it.users,
                    if (it.previous != null) key - 1 else null,
                    if (it.previous != null) key + 1 else null
                )
            }
            .onErrorReturn { e ->
                LoadResult.Error(e)
            }
    }
}
