package com.intergroupapplication.data.remote_mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.rxjava2.RxRemoteMediator
import com.intergroupapplication.data.db.IgDatabase
import com.intergroupapplication.data.db.entity.GroupPostDb
import com.intergroupapplication.data.db.entity.GroupPostRemoteKeysDb
import com.intergroupapplication.data.mappers.group.GroupPostDtoToDbMapper
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.network.dto.GroupPostsDto
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers


@ExperimentalPagingApi
class GroupPostMediatorRXDataSource(
    private val appApi: AppApi,
    private val appDatabase: IgDatabase,
    private val groupId: String,
    private val groupPostMapper: GroupPostDtoToDbMapper
) : RxRemoteMediator<Int, GroupPostDb>() {

    private companion object {
        const val INVALID_PAGE = -1
        const val EMPTY_MODEL = -2
    }

    override fun loadSingle(loadType: LoadType, state: PagingState<Int, GroupPostDb>)
            : Single<MediatorResult> {
        return Single.just(loadType)
            .subscribeOn(Schedulers.io())
            .map {
                when (it) {
                    LoadType.REFRESH -> {
                        val model = getRemoteKeyClosestToCurrentPosition(state)
                        model?.nextKey?.minus(1) ?: 1
                    }
                    LoadType.PREPEND -> {
                        val groupModel = getRemoteKeyForFirstItem(state)
                        groupModel?.prevKey ?: INVALID_PAGE
                    }
                    LoadType.APPEND -> {
                        val model = getRemoteKeyForLastItem(state)
                        model ?: return@map EMPTY_MODEL
                        model.nextKey ?: INVALID_PAGE
                    }
                }
            }
            .flatMap { page ->
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
                            .onErrorReturn {
                                MediatorResult.Error(it)
                            }
                    }
                }
            }
            .onErrorReturn {
                MediatorResult.Error(it)
            }
    }

    private fun insertToDb(page: Int, loadType: LoadType, data: GroupPostsDto): GroupPostsDto {

        if (loadType == LoadType.REFRESH) {
            appDatabase.groupPostDao().clearAllGroupPosts(groupId)
            appDatabase.groupPostKeyDao().deleteByGroupId(groupId)
        }

        val prevKey = if (page == 1) null else page - 1
        val nextKey = if (data.next == null) null else page + 1
        val key = GroupPostRemoteKeysDb(groupId, prevKey, nextKey)
        appDatabase.groupPostKeyDao().insertKey(key)
        appDatabase.groupPostDao().insertAll(data.posts.map(groupPostMapper))
        return data
    }

    private fun getRemoteKeyForLastItem(state: PagingState<Int, GroupPostDb>)
            : GroupPostRemoteKeysDb? {
        return state.lastItemOrNull()?.let { group ->
            appDatabase.groupPostKeyDao().getRemoteKey(group.groupInPost.id)
        }
    }

    private fun getRemoteKeyForFirstItem(state: PagingState<Int, GroupPostDb>)
            : GroupPostRemoteKeysDb? {
        return state.firstItemOrNull()?.let { group ->
            appDatabase.groupPostKeyDao().getRemoteKey(group.groupInPost.id)
        }
    }

    private fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, GroupPostDb>)
            : GroupPostRemoteKeysDb? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.groupInPost?.id?.let { id ->
                appDatabase.groupPostKeyDao().getRemoteKey(id)
            }
        }
    }
}