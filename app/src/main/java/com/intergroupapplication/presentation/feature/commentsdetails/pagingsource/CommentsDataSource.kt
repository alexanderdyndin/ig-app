package com.intergroupapplication.presentation.feature.commentsdetails.pagingsource

import androidx.paging.PageKeyedDataSource
import android.util.Log
import com.intergroupapplication.di.scope.PerActivity
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.domain.entity.CommentEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.gateway.CommentGateway
import com.intergroupapplication.domain.gateway.GroupPostGateway
import com.intergroupapplication.presentation.base.BasePagingState
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

@PerFragment
class CommentsDataSource @Inject constructor(private val commentsGateway: CommentGateway,
                                             private val compositeDisposable: CompositeDisposable)
    : PageKeyedDataSource<Int, CommentEntity>() {

    var postId: String = ""
    var currentPage = 1
    private val subject: PublishSubject<BasePagingState> = PublishSubject.create()
    private var retryAction: (() -> Unit)? = null

    fun reload() = retryAction?.invoke()

    fun observeState(): Observable<BasePagingState> = subject

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, CommentEntity>) {
        compositeDisposable.add(commentsGateway.getComments(postId, currentPage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { subject.onNext(BasePagingState(BasePagingState.Type.LOADING)) }
                .doOnSuccess { subject.onNext(BasePagingState(BasePagingState.Type.NONE)) }
                .subscribe({
                    callback.onResult(it, getBeforePage(), getAfterPage())
                    retryAction = null
                }, {
                    subject.onNext(BasePagingState(BasePagingState.Type.ERROR, it))
                    retryAction = { loadInitial(params, callback) }
                }))
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, CommentEntity>) {
        compositeDisposable.add(commentsGateway.getComments(postId, params.key)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { subject.onNext(BasePagingState(BasePagingState.Type.LOADING)) }
                .doOnSuccess { subject.onNext(BasePagingState(BasePagingState.Type.NONE)) }
                .subscribe({
                    callback.onResult(it, params.key + 1)
                    retryAction = null
                }, {
                    subject.onNext(BasePagingState(BasePagingState.Type.ERROR, it))
                    retryAction = { loadAfter(params, callback) }
                }))
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, CommentEntity>) {
        compositeDisposable.add(commentsGateway.getComments(postId, params.key)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { subject.onNext(BasePagingState(BasePagingState.Type.LOADING)) }
                .doOnSuccess { subject.onNext(BasePagingState(BasePagingState.Type.NONE)) }
                .subscribe({
                    callback.onResult(it, params.key - 1)
                    retryAction = null
                }, {
                    subject.onNext(BasePagingState(BasePagingState.Type.ERROR, it))
                    retryAction = { loadBefore(params, callback) }
                }))
    }

    fun newPage() {

    }

    private fun getBeforePage() = if (currentPage > 1) currentPage - 1 else null

    private fun getAfterPage() = currentPage + 1


}