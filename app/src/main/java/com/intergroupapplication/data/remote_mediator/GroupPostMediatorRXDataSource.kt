package com.intergroupapplication.data.remote_mediator

import com.intergroupapplication.data.model.GroupPostModel
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.rxjava2.RxRemoteMediator
import com.intergroupapplication.data.db.IgDatabase
import com.intergroupapplication.data.network.AppApi
import io.reactivex.Single
import com.intergroupapplication.data.db.entity.GroupPostRemoteKeysModel
import com.intergroupapplication.data.model.GroupPostsDto
import io.reactivex.schedulers.Schedulers
import timber.log.Timber


@ExperimentalPagingApi
class GroupPostMediatorRXDataSource(
        private val appApi: AppApi,
        private val appDatabase: IgDatabase,
        private val groupId: String
) : RxRemoteMediator<Int, GroupPostModel>() {

    private companion object {
        const val INVALID_PAGE = -1
        const val EMPTY_MODEL = -2
    }

    override fun loadSingle(loadType: LoadType, state: PagingState<Int, GroupPostModel>)
        : Single<MediatorResult> {
        return Single.just(loadType)
                .subscribeOn(Schedulers.io())
                .map {
                    when (it) {
                        LoadType.REFRESH -> {
                            val model = getRemoteKeyClosestToCurrentPosition(state)
                            Timber.tag("tut_refresh").d(model.toString())
                            model?.nextKey?.minus(1) ?: 1
                        }
                        LoadType.PREPEND -> {
                            val groupModel = getRemoteKeyForFirstItem(state)
                            Timber.tag("tut_prepend").d(groupModel.toString())
                            groupModel?.prevKey ?: INVALID_PAGE
                        }
                        LoadType.APPEND -> {
                            val model = getRemoteKeyForLastItem(state)
                            Timber.tag("tut_append").d(model.toString())
                            model?: return@map EMPTY_MODEL
                            model.nextKey ?: INVALID_PAGE
                        }
                    }
                }
                .flatMap { page ->
                    Timber.tag("tut_page").d(page.toString())
                    when (page) {
                        INVALID_PAGE -> {
                            Single.just(MediatorResult.Success(true))
                        }
                        EMPTY_MODEL -> {
                            Single.just(MediatorResult.Success(false))
                        }
                        else -> {
                            appApi.getGroupPosts(groupId, page)
                                .map { data ->
                                    insertToDb(page, loadType, data)
                                }
                                .map<MediatorResult> {
                                    MediatorResult.Success(it.next == null)
                                }
                                .onErrorReturn{ MediatorResult.Error(it)}
                        }
                    }
                }.onErrorReturn { MediatorResult.Error(it) }
    }

    private fun insertToDb(page: Int, loadType: LoadType, data: GroupPostsDto): GroupPostsDto {

        if (loadType == LoadType.REFRESH) {
            appDatabase.groupPostDao().clearAllGroupPosts(groupId)
            appDatabase.groupPostKeyDao().deleteByGroupId(groupId)
        }

        val prevKey = if (page == 1) null else page - 1
        val nextKey = if (data.next == null) null else page + 1
        val key = GroupPostRemoteKeysModel(groupId, prevKey, nextKey)
        appDatabase.groupPostKeyDao().insertKey(key)
        appDatabase.groupPostDao().insertAll(
                data.groupPostModels.map { groupPostModel ->
                    groupPostModel.groupId = groupId
                    groupPostModel
                }
        )
        return data
    }

    private fun getRemoteKeyForLastItem(state: PagingState<Int, GroupPostModel>)
            : GroupPostRemoteKeysModel? {
        return state.lastItemOrNull()?.let {  group ->
            appDatabase.groupPostKeyDao().getRemoteKey(group.groupId)
        }
    }

    private fun getRemoteKeyForFirstItem(state: PagingState<Int, GroupPostModel>)
            : GroupPostRemoteKeysModel? {
        return state.firstItemOrNull()?.let { group ->
            appDatabase.groupPostKeyDao().getRemoteKey(group.groupId)
        }
    }

    private fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, GroupPostModel>)
        : GroupPostRemoteKeysModel? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.groupId?.let { id ->
                appDatabase.groupPostKeyDao().getRemoteKey(id)
            }
        }
    }
}