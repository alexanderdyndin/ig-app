package com.intergroupapplication.presentation.feature.news.pagingsource

import androidx.paging.PagingSource
import androidx.paging.rxjava2.RxPagingSource
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.entity.NewsEntity
import com.intergroupapplication.domain.gateway.GroupPostGateway
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class NewsDataSource3 @Inject constructor(private val groupPostGateway: GroupPostGateway,
                                          private val compositeDisposable: CompositeDisposable
                      ): RxPagingSource<Int, GroupPostEntity>() {

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, GroupPostEntity>> {
        val position = params.key ?: 1
        return groupPostGateway.getNewsPosts(position)
                .subscribeOn(Schedulers.io())
                .map { toLoadResult(it, position) }
                .onErrorReturn { LoadResult.Error(it) }
    }

    private fun toLoadResult(data: NewsEntity, position: Int): LoadResult<Int, GroupPostEntity> {
        return LoadResult.Page(
                data = data.news,
                prevKey = data.previous,
                nextKey = data.next
        )
    }

}