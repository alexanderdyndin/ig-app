package com.intergroupapplication.data.remote_mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.rxjava2.RxRemoteMediator
import com.intergroupapplication.data.db.IgDatabase
import com.intergroupapplication.data.db.entity.NewsPostDb
import com.intergroupapplication.data.db.entity.NewsPostRemoteKeyEntity
import com.intergroupapplication.data.mapper.NewsModelToNewsPostDbMapper
import com.intergroupapplication.data.model.NewsDto
import com.intergroupapplication.data.network.AppApi
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

@ExperimentalPagingApi
class NewsPostMediatorRXDataSource(
    private val appApi: AppApi,
    private val igDatabase: IgDatabase,
    private val newsModelToDbMapper: NewsModelToNewsPostDbMapper
) : RxRemoteMediator<Int, NewsPostDb>() {

    private companion object {
        const val INVALID_PAGE = -1
        const val EMPTY_MODEL = -2
    }

    override fun loadSingle(
        loadType: LoadType,
        state: PagingState<Int, NewsPostDb>
    ): Single<MediatorResult> {
        return Single.just(loadType)
            .map {
                when (it) {
                    LoadType.REFRESH -> {
                        val entity = getRemoteKeyClosestToCurrentPosition(state)
                        entity?.nextKey?.minus(1) ?: 1
                    }
                    LoadType.PREPEND -> {
                        val entity = getRemoteKeyForFirstItem(state)
                        entity?.prevKey ?: INVALID_PAGE
                    }
                    LoadType.APPEND -> {
                        val entity = getRemoteKeyForLastItem(state)
                        entity ?: return@map EMPTY_MODEL
                        entity.nextKey ?: INVALID_PAGE
                    }
                }
            }
            .flatMap { page ->
                when (page) {
                    EMPTY_MODEL -> {
                        Single.just(MediatorResult.Success(false))
                    }
                    INVALID_PAGE -> {
                        Single.just(MediatorResult.Success(true))
                    }
                    else -> {
                        appApi.getNews(page)
                            .map { data ->
                                insertToDb(page, loadType, data)
                            }
                            .map<MediatorResult> {
                                MediatorResult.Success(it.next == null)
                            }
                            .onErrorReturn { MediatorResult.Error(it) }
                    }
                }
            }
            .subscribeOn(Schedulers.io())
            .onErrorReturn { MediatorResult.Error(it) }
    }

    private fun insertToDb(page: Int, loadType: LoadType, data: NewsDto): NewsDto {

        if (loadType == LoadType.REFRESH) {
            igDatabase.newsPostDao().clearAllNewsPost()
        }

        val prevKey = if (page == 1) null else page - 1
        val nextKey = if (data.next == null) null else page + 1
        val key = NewsPostRemoteKeyEntity(prevKey = prevKey, nextKey = nextKey)
        if (loadType != LoadType.PREPEND) {
            igDatabase.newsPostKeyDao().insertKey(key)
        }
        igDatabase.newsPostDao().insertAll(data.news.map(newsModelToDbMapper))
        return data
    }


    private fun getRemoteKeyForLastItem(state: PagingState<Int, NewsPostDb>)
            : NewsPostRemoteKeyEntity? {
        return state.lastItemOrNull()?.let { _ ->
            igDatabase.newsPostKeyDao().getRemoteKey()
        }
    }

    private fun getRemoteKeyForFirstItem(state: PagingState<Int, NewsPostDb>)
            : NewsPostRemoteKeyEntity? {
        return state.firstItemOrNull()?.let { _ ->
            igDatabase.newsPostKeyDao().getRemoteKey()
        }
    }

    private fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, NewsPostDb>)
            : NewsPostRemoteKeyEntity? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.let { _ ->
                igDatabase.newsPostKeyDao().getRemoteKey()
            }
        }
    }
}
