package com.intergroupapplication.presentation.feature.grouplist.pagingsource

import androidx.paging.PageKeyedDataSource
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.domain.gateway.GroupGateway
import com.intergroupapplication.presentation.base.BasePagingState
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class AdminGroupListDataSource @Inject constructor(private val groupGateway: GroupGateway,
                                                   private val compositeDisposable: CompositeDisposable) :
        PageKeyedDataSource<Int, GroupEntity>() {

    companion object {
        const val FIRST_PAGE = 1
        const val SECOND_PAGE = 2
    }
    var search = ""

    private val subject: PublishSubject<BasePagingState> = PublishSubject.create()
    private var retryAction: (() -> Unit)? = null

    fun reload() = retryAction?.invoke()

    fun observeState(): Observable<BasePagingState> = subject

    fun applySearchFilter(query:String){
        search = query
    }


    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, GroupEntity>) {
        compositeDisposable.add(groupGateway.getAdminGroupList(FIRST_PAGE, search)
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

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, GroupEntity>) {
        compositeDisposable.add(groupGateway.getAdminGroupList(params.key, search)
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

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, GroupEntity>) = Unit

}
