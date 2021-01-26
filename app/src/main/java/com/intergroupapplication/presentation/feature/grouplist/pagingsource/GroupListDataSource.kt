package com.intergroupapplication.presentation.feature.grouplist.pagingsource

import androidx.paging.PageKeyedDataSource
import com.intergroupapplication.domain.FakeData
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.domain.entity.GroupListEntity
import com.intergroupapplication.domain.gateway.GroupGateway
import com.intergroupapplication.presentation.base.BasePagingState
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class GroupListDataSource @Inject constructor(private val groupGateway: GroupGateway,
                                              private val compositeDisposable: CompositeDisposable) :
        PageKeyedDataSource<Int, GroupEntity>() {

    companion object {
        const val FIRST_PAGE = 1
    }
    private var search = ""

    private var getGroupList: (Int, String) -> Single<GroupListEntity> = { page: Int, search: String ->
        groupGateway.getGroupList(page, search)
    }

    private val subject: PublishSubject<BasePagingState> = PublishSubject.create()
    private var retryAction: (() -> Unit)? = null

    fun reload() = retryAction?.invoke()


    fun observeState(): Observable<BasePagingState> = subject

    fun applySearchFilter(query:String){
        search = query
    }

    fun applyAllGroupList() {
        getGroupList = { page: Int, search: String ->
            groupGateway.getGroupList(page, search)
        }
    }

    fun applySubscribedGroupList() {
        getGroupList = { page: Int, search: String ->
            groupGateway.getSubscribedGroupList(page, search)
        }
    }

    fun applyOwnedGroupList() {
        getGroupList = { page: Int, search: String ->
            groupGateway.getAdminGroupList(page, search)
        }
    }

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, GroupEntity>) {
        compositeDisposable.add(getGroupList.invoke(FIRST_PAGE, search)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { subject.onNext(BasePagingState(BasePagingState.Type.LOADING)) }
                .doOnSuccess { subject.onNext(BasePagingState(BasePagingState.Type.NONE, null, it.count)) }
                .subscribe({
                    callback.onResult(it.groups, it.previous, it.next)
                    retryAction = null
                }, {
                    subject.onNext(BasePagingState(BasePagingState.Type.ERROR, it))
                    retryAction = { loadInitial(params, callback) }
                }))

    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, GroupEntity>) {
        compositeDisposable.add(getGroupList.invoke(params.key, search)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { subject.onNext(BasePagingState(BasePagingState.Type.LOADING)) }
                .doOnSuccess { subject.onNext(BasePagingState(BasePagingState.Type.NONE, null, it.count)) }
                .subscribe({
                    callback.onResult(it.groups, it.next)
                    retryAction = null
                }, {
                    subject.onNext(BasePagingState(BasePagingState.Type.ERROR, it))
                    retryAction = { loadAfter(params, callback) }
                }))
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, GroupEntity>) = Unit

}
