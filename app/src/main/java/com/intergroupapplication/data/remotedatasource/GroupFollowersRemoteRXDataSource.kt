package com.intergroupapplication.data.remotedatasource

import androidx.paging.PagingState
import androidx.paging.rxjava2.RxPagingSource
import com.intergroupapplication.data.mapper.FollowersGroupMapper
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.domain.entity.UserEntity
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class GroupFollowersRemoteRXDataSource (
        private val appApi: AppApi,
        private val mapper: FollowersGroupMapper,
        private val groupId: String): RxPagingSource<Int, UserEntity>() {

    override fun getRefreshKey(state: PagingState<Int, UserEntity>): Int? {
        val position = state.anchorPosition ?: 0
        return position / 20 + 1
    }

    var key: Int = 1

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, UserEntity>> {
        return appApi.getGroupFollowers(groupId = groupId)
                .subscribeOn(Schedulers.io())
                .map {
                    mapper.mapToDomainEntity(it)
                }
                .map <LoadResult<Int, UserEntity>> {
                    LoadResult.Page(
                            it.users,
                            if (it.previous != null) key - 1 else null,
                            if (it.previous != null) key + 1 else null)
                }
                .onErrorReturn { e ->
                    LoadResult.Error(e)
                }
    }
}