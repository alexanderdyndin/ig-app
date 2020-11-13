package com.intergroupapplication.presentation.feature.news.pagingsource

import androidx.paging.PageKeyedDataSource
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.gateway.GroupPostGateway
import com.intergroupapplication.presentation.base.BasePagingState
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 16/10/2018 at project InterGroupApplication.
 */
class NewsDataSource @Inject constructor(private val groupPostGateway: GroupPostGateway,
                                         private val compositeDisposable: CompositeDisposable)
    : PageKeyedDataSource<Int, GroupPostEntity>() {

    companion object {
        const val FIRST_PAGE = 1
        const val SECOND_PAGE = 2
    }

    private val subject: PublishSubject<BasePagingState> = PublishSubject.create()
    private var retryAction: (() -> Unit)? = null

    lateinit var list: List<GroupPostEntity>

    fun reload() = retryAction?.invoke()

    fun observeState(): Observable<BasePagingState> = subject

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, GroupPostEntity>) {

        compositeDisposable.add(groupPostGateway.getNewsPosts(FIRST_PAGE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { subject.onNext(BasePagingState(BasePagingState.Type.LOADING)) }
                .doOnSuccess {
                    subject.onNext(BasePagingState(BasePagingState.Type.NONE))
                }
                .subscribe({
                    if (it.next == null) {
                        callback.onResult(it.news, it.previous, it.next)
                    } else {
                        callback.onResult(it.news, it.previous, SECOND_PAGE)
                    }
                    list = it.news
                    retryAction = null
                }, {
                    subject.onNext(BasePagingState(BasePagingState.Type.ERROR, it))
                    retryAction = { loadInitial(params, callback) }
                }))
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, GroupPostEntity>) {
        compositeDisposable.add(groupPostGateway.getNewsPosts(params.key)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { subject.onNext(BasePagingState(BasePagingState.Type.LOADING)) }
                .doOnSuccess { subject.onNext(BasePagingState(BasePagingState.Type.NONE)) }
                .subscribe({
                    if (it.next == null) {
                        callback.onResult(it.news, it.next)
                    } else {
                        callback.onResult(it.news, params.key + 1)
                    }
                    list = it.news
                    retryAction = null
                }, {
                    subject.onNext(BasePagingState(BasePagingState.Type.ERROR, it))
                    retryAction = { loadAfter(params, callback) }
                }))
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, GroupPostEntity>) {
//        compositeDisposable.add(groupPostGateway.getNewsPosts(params.key)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .doOnSubscribe { subject.onNext(BasePagingState(BasePagingState.Type.LOADING)) }
//                .doOnSuccess { subject.onNext(BasePagingState(BasePagingState.Type.NONE)) }
//                .subscribe({
//                    callback.onResult(it.news, it.previous)
//                    retryAction = null
//                }, {
//                    subject.onNext(BasePagingState(BasePagingState.Type.ERROR, it))
//                    retryAction = { loadAfter(params, callback) }
//                }))
    }

}