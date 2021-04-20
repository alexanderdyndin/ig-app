package com.intergroupapplication.data.remotedatasource

import androidx.paging.PagingState
import androidx.paging.rxjava2.RxPagingSource
import com.intergroupapplication.data.mapper.FollowersGroupMapper
import com.intergroupapplication.data.model.group_followers.GroupUserFollowersDto
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.domain.entity.GroupUserEntity
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class GroupFollowersRemoteRXDataSource (
        private val appApi: AppApi,
        private val mapper: FollowersGroupMapper,
        private val groupId: String,
        private val searchFilter: String
): RxPagingSource<Int, GroupUserEntity>() {

    var key: Int = 1

    private var followersList: (Int) -> Single<GroupUserFollowersDto> = { page: Int ->
        appApi.getGroupFollowers(groupId, page, searchFilter, searchFilter)
    }

    override fun getRefreshKey(state: PagingState<Int, GroupUserEntity>): Int? {
        val position = state.anchorPosition ?: 0
        return position / 20 + 1
    }

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, GroupUserEntity>> {
        key = params.key ?: 1
        return followersList
                .invoke(key)
                .subscribeOn(Schedulers.io())
                .map {
                    mapper.mapToDomainEntity(it)
                }
                .map <LoadResult<Int, GroupUserEntity>> {
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

    fun applyAdministratorsList() {
        followersList = {page: Int ->
            appApi.getGroupFollowers(groupId, page, "admins", searchFilter)
        }
    }
}