package com.intergroupapplication.data.remotedatasource

import androidx.paging.PagingState
import androidx.paging.rxjava2.RxPagingSource
import com.intergroupapplication.data.mapper.GroupMapper
import com.intergroupapplication.data.model.GroupsDto
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.network.PAGE_SIZE
import com.intergroupapplication.domain.entity.GroupEntity
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class GroupsRemoteRXDataSource (private val appApi: AppApi,
                                private val mapper: GroupMapper,
                                private val query: String): RxPagingSource<Int, GroupEntity>() {

    override val jumpingSupported = true

    var getGroupList: (Int) -> Single<GroupsDto> = { page: Int ->
        appApi.getGroupList(page, query)
    }

    fun applyAllGroupList() {
        getGroupList = { page: Int ->
            appApi.getGroupList(page, query)
        }
    }

    fun applySubscribedGroupList() {
        getGroupList = { page: Int ->
            appApi.getSubscribedGroupList(page, query)
        }
    }

    fun applyAdminGroupList() {
        getGroupList = { page: Int ->
            appApi.getAdminGroupList(page, query)
        }
    }

    var key: Int = 1

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, GroupEntity>> {
        key = params.key?: 1
        return getGroupList.invoke(params.key?: 1)
                .subscribeOn(Schedulers.io())
                .map { mapper.mapToDomainEntity(it) }
                .map <LoadResult<Int, GroupEntity>> {
                    LoadResult.Page(it.groups,
                            if (it.previous != null) key - 1 else null,
                            if (it.next != null) key + 1 else null)
                }
                .onErrorReturn { e ->
                    LoadResult.Error(e)
                }
    }

    override fun getRefreshKey(state: PagingState<Int, GroupEntity>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            // This loads starting from previous page, but since PagingConfig.initialLoadSize spans
            // multiple pages, the initial load will still load items centered around
            // anchorPosition. This also prevents needing to immediately launch prepend due to
            // prefetchDistance.
            (state.closestPageToPosition(anchorPosition)?.prevKey ?: 0) + 1
        }
    }

}