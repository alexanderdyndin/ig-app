package com.intergroupapplication.data.remotedatasource

import androidx.paging.PagingState
import androidx.paging.rxjava2.RxPagingSource
import com.intergroupapplication.data.mappers.CommentMapper
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.domain.entity.CommentEntity
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class CommentsRemoteRXDataSource(
    private val appApi: AppApi,
    private val mapper: CommentMapper,
    private val postId: String,
    private val page: Int = 1
) : RxPagingSource<Int, CommentEntity>() {

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, CommentEntity>> {
        val key = params.key ?: page
        return appApi.getPostComments(postId, key)
            .subscribeOn(Schedulers.io())
            .map { mapper.mapToDomainEntity(it) }
            .map<LoadResult<Int, CommentEntity>> {
                LoadResult.Page(
                    it.comments,
                    if (it.previous != null) key - 1 else null,
                    if (it.next != null) key + 1 else null
                )
            }
            .onErrorReturn { e ->
                LoadResult.Error(e)
            }
    }

    override fun getRefreshKey(state: PagingState<Int, CommentEntity>): Int? {
        //return if (state.anchorPosition != null) state.anchorPosition!! / 20 + 1 else null
        return null
    }
}
