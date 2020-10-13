package com.intergroupapplication.presentation.feature.group.pagingsource

import androidx.paging.PageKeyedDataSource
import com.intergroupapplication.di.scope.PerActivity
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
 * Created by abakarmagomedov on 19/09/2018 at project InterGroupApplication.
 */
@PerActivity
class GroupPostDataSource @Inject constructor(private val groupPostGateway: GroupPostGateway,
                                              private val compositeDisposable: CompositeDisposable)
    : PageKeyedDataSource<Int, GroupPostEntity>() {

    companion object {
        const val FIRST_PAGE = 1
        const val SECOND_PAGE = 2
    }

    var groupId: String = ""
    private val subject: PublishSubject<BasePagingState> = PublishSubject.create()
    private var retryAction: (() -> Unit)? = null

    fun reload() = retryAction?.invoke()

    fun observeState(): Observable<BasePagingState> = subject

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, GroupPostEntity>) {
        compositeDisposable.add(groupPostGateway.getGroupPosts(groupId, FIRST_PAGE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { subject.onNext(BasePagingState(BasePagingState.Type.LOADING)) }
                .doOnSuccess { subject.onNext(BasePagingState(BasePagingState.Type.NONE)) }
                .subscribe({
                    callback.onResult(it, null, SECOND_PAGE)
                    retryAction = null
                }, {
                    subject.onNext(BasePagingState(BasePagingState.Type.ERROR, it))
                    retryAction = { loadInitial(params, callback) }
                }))
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, GroupPostEntity>) {
        compositeDisposable.add(groupPostGateway.getGroupPosts(groupId, params.key)
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

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, GroupPostEntity>) = Unit

}