package com.intergroupapplication.data.remote_mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.rxjava2.RxRemoteMediator
import com.intergroupapplication.data.db.IgDatabase
import com.intergroupapplication.data.db.entity.GroupPostDbModel
import com.intergroupapplication.data.mapper.GroupPostMapper
import com.intergroupapplication.data.network.AppApi
import io.reactivex.Single
import androidx.paging.PagingSource.*

import androidx.paging.RemoteMediator.*

import com.intergroupapplication.data.db.entity.GroupPostRemoteKeysModel


@ExperimentalPagingApi
class GroupPostMediatorRXDataSource(
        private val appApi: AppApi,
        private val appDatabase: IgDatabase,
        private val mapper: GroupPostMapper,
        private val groupId: String
) : RxRemoteMediator<Int, GroupPostDbModel>() {

    override fun initializeSingle(): Single<InitializeAction> {
        return Single.just(InitializeAction.LAUNCH_INITIAL_REFRESH)
    }

    override fun loadSingle(loadType: LoadType, state: PagingState<Int, GroupPostDbModel>): Single<MediatorResult> {
        val remoteKeySingle: Single<GroupPostRemoteKeysModel> = when (loadType) {
            LoadType.REFRESH -> {
                Single.just(GroupPostRemoteKeysModel(groupId, null, null))
            }
            LoadType.PREPEND -> return Single.just(MediatorResult.Success(true))
            LoadType.APPEND -> appDatabase.groupPostKey().getRemoteKey(groupId)
        }

        return remoteKeySingle.flatMap { remoteKey ->
            if (loadType != LoadType.REFRESH && remoteKey.next == null) {
                return@flatMap Single.just(MediatorResult.Success(true))
            }

            return@flatMap appApi.getGroupPosts(groupId, remoteKey.next ?: 1)
                    .map { response ->
                        appDatabase.runInTransaction {
                            if (loadType == LoadType.REFRESH) {
                                appDatabase.groupPost().clearAllGroupPosts(groupId)
                                appDatabase.groupPostKey().deleteByGroupId(groupId)
                            }

                            appDatabase.run {
                                groupPostKey().insertOrReplace(GroupPostRemoteKeysModel(groupId, response.next.toInt(), response.previous.toInt()))
                                groupPost().insertAll(
                                        response.results.map {
                                            mapper.mapResponseToModelDb(it, groupId)
                                        }
                                )
                            }
                        }

                        return@map MediatorResult.Success(response.next == null)
                    }
        }

    }
}

