package com.intergroupapplication.data.remote_mediator

import com.intergroupapplication.data.model.GroupPostModel
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.rxjava2.RxRemoteMediator
import com.intergroupapplication.data.db.IgDatabase
import com.intergroupapplication.data.mapper.GroupPostMapper
import com.intergroupapplication.data.network.AppApi
import io.reactivex.Single
import androidx.paging.PagingSource.*
import androidx.paging.RemoteMediator.*
import com.intergroupapplication.data.db.entity.GroupPostRemoteKeysModel
import io.reactivex.schedulers.Schedulers


@ExperimentalPagingApi
class GroupPostMediatorRXDataSource(
        private val appApi: AppApi,
        private val appDatabase: IgDatabase,
        private val mapper: GroupPostMapper,
        private val groupId: String
) : RxRemoteMediator<Int, GroupPostModel>() {

    override fun initializeSingle(): Single<InitializeAction> {
        return Single.just(InitializeAction.LAUNCH_INITIAL_REFRESH)
    }

    override fun loadSingle(loadType: LoadType, state: PagingState<Int, GroupPostModel>): Single<MediatorResult> {
        val remoteKeySingle: Single<GroupPostRemoteKeysModel> = when (loadType) {
            LoadType.REFRESH -> {
                Single.just(GroupPostRemoteKeysModel(groupId, null, null))
            }
            LoadType.PREPEND -> return Single.just(MediatorResult.Success(true))
            LoadType.APPEND -> appDatabase.groupPostKeyDao().getRemoteKey(groupId)
        }

        return remoteKeySingle
                .subscribeOn(Schedulers.io())
                .flatMap { remoteKey ->
                    if (loadType != LoadType.REFRESH && remoteKey.next == null) {
                        return@flatMap Single.just(MediatorResult.Success(true))
                    }

                    return@flatMap appApi.getGroupPosts(groupId, remoteKey.next ?: 1)
                            .map { response ->
                                appDatabase.runInTransaction {
                                    if (loadType == LoadType.REFRESH) {
                                        appDatabase.groupPostDao().clearAllGroupPosts(groupId)
                                        appDatabase.groupPostKeyDao().deleteByGroupId(groupId)
                                    }

                                    appDatabase.run {
                                        groupPostKeyDao().insertOrReplace(GroupPostRemoteKeysModel(
                                                groupId,
                                                response.next.toInt(),
                                                response.previous.toInt())
                                        )

                                        groupPostDao().insertAll(
                                                response.results.map { groupPostModel ->
                                                    groupPostModel.groupId = groupId
                                                    groupPostModel
                                                }
                                        )
                                    }

                                }
                                MediatorResult.Success(response.next.isNotEmpty())
                            }
                }
    }
}

