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


@ExperimentalPagingApi
class GroupPostMediatorRXDataSource(
        private val appApi: AppApi,
        private val appDatabase: IgDatabase,
        private val groupId: String
) : RxRemoteMediator<Int, GroupPostModel>() {

    companion object {
        const val INVALID_PAGE = -1
    }

    override fun loadSingle(loadType: LoadType, state: PagingState<Int, GroupPostModel>): Single<MediatorResult> {
        return Single.just(loadType)
                .subscribeOn(Schedulers.io())
                .map {
                    when (it) {
                        LoadType.REFRESH -> {
                            val position = appDatabase.groupPostKeyDao().getRemoteKey(groupId)
                            position?.next ?: 1
                        }
                        LoadType.PREPEND -> {
                            INVALID_PAGE
                        }
                        LoadType.APPEND -> {
                            appDatabase.groupPostKeyDao().getRemoteKey(groupId)?.next ?: INVALID_PAGE
                        }
                    }
                }
                .flatMap { page ->
                    if (page == INVALID_PAGE) {
                        Single.just(MediatorResult.Success(true))
                    } else {
                        appApi.getGroupPosts(groupId, page)
                                .map { data ->
                                    insertToDb(page, loadType, data)
                                }
                                .map<MediatorResult> {
                                    MediatorResult.Success(it.next == null)
                                }
                    }

                }
    }

    private fun insertToDb(page: Int, loadType: LoadType, data: GroupPostsDto): GroupPostsDto {

        if (loadType == LoadType.REFRESH) {
            appDatabase.groupPostDao().clearAllGroupPosts(groupId)
            appDatabase.groupPostKeyDao().deleteByGroupId(groupId)
        }

        val next =
                if (data.next != null) page.plus(1)
                else null

        val keys = GroupPostRemoteKeysModel(
                groupId,
                next
        )
        appDatabase.groupPostKeyDao().insertOrReplace(keys)
        appDatabase.groupPostDao().insertAll(
                data.results.map { groupPostModel ->
                    groupPostModel.groupId = groupId
                    groupPostModel
                }
        )
        return data
    }
}