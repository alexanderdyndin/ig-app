package com.intergroupapplication.presentation.feature.grouplist.presenter

import android.util.Log
import androidx.fragment.app.FragmentManager
import androidx.paging.RxPagedListBuilder
import com.androidnetworking.core.MainThreadExecutor
import moxy.InjectViewState
import com.intergroupapplication.BuildConfig
import com.intergroupapplication.domain.FakeData
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.domain.exception.PageNotFoundException
import com.intergroupapplication.domain.gateway.GroupGateway
import com.intergroupapplication.domain.usecase.AppStatusUseCase
import com.intergroupapplication.presentation.base.BasePagingState
import com.intergroupapplication.presentation.base.BasePagingState.Companion.PAGINATION_PAGE_SIZE
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.exstension.handleLoading
import com.intergroupapplication.presentation.feature.group.view.GroupScreen
import com.intergroupapplication.presentation.feature.grouplist.pagingsource.*
import com.intergroupapplication.presentation.feature.grouplist.view.GroupListView
import com.intergroupapplication.presentation.feature.newVersionDialog.NewVersionDialog
import com.workable.errorhandler.ErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.Screen
import javax.inject.Inject

@InjectViewState
class GroupListPresenter @Inject constructor(private val router: Router,
                                             private val errorHandler: ErrorHandler,
                                             private val appStatusUseCase: AppStatusUseCase)
    : BasePresenter<GroupListView>() {


    private val groupsDisposable = CompositeDisposable()

    @Inject
    lateinit var dsAll: AllGroupListDataSourceFactory

    @Inject
    lateinit var dsSub: SubscribedGroupListDataSourceFactory

    @Inject
    lateinit var dsAdm: AdminGroupListDataSourceFactory

    @Inject
    lateinit var groupGateway: GroupGateway


    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        groupList()
    }

    fun checkNewVersionAvaliable(fragmentManager: FragmentManager) {

        GlobalScope.launch {
            try {
                val isValid = appStatusUseCase.invoke(BuildConfig.VERSION_NAME).blockingGet()
                Log.d("MY", "version_check_response = $isValid ")
                if (isValid == "invalid") {
                    val myDialogFragment = NewVersionDialog()
                    myDialogFragment.isCancelable = false
                    myDialogFragment.show(fragmentManager, "myDialog")
                }
            }  catch (e:Throwable) {
                errorHandler.handle(e)
            }
        }
    }

    fun groupList() {
        getGroupsList()
        getFollowGroupsList()
        getOwnedGroupsList()
    }

    fun getGroupsList() {
        groupsDisposable.add(dsAll.source.observeState()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .handleLoading(viewState)
                .subscribe({
                    it.error?.let { throwable ->
                        errorHandler.handle(throwable)
                }
                        //todo исправить пагинацию
                        if (it.error !is PageNotFoundException) {
                            viewState.handleState(it.type)
                        } else {
                            viewState.handleState(BasePagingState.Type.NONE)
                        }
    }, {}))
        groupsDisposable.add(RxPagedListBuilder(dsAll, PAGINATION_PAGE_SIZE)
                .buildObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    viewState.groupListLoaded(it)
                }, {
                    errorHandler.handle(it)
                }))
    }

    fun getFollowGroupsList() {
        groupsDisposable.add(dsSub.source.observeState()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .handleLoading(viewState)
                .subscribe({
                    it.error?.let { throwable ->
                        errorHandler.handle(throwable)
                    }
                    //todo исправить пагинацию
                    if (it.error !is PageNotFoundException) {
                        viewState.handleState1(it.type)
                    } else {
                        viewState.handleState1(BasePagingState.Type.NONE)
                    }
                }, {}))

        groupsDisposable.add(RxPagedListBuilder(dsSub, PAGINATION_PAGE_SIZE)
                .buildObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    viewState.groupListSubLoaded(it)
                }, {
                    errorHandler.handle(it)
                }))
    }

    fun getOwnedGroupsList() {
        groupsDisposable.add(dsAdm.source.observeState()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .handleLoading(viewState)
                .subscribe({
                    it.error?.let { throwable ->
                        errorHandler.handle(throwable)
                    }
                    //todo исправить пагинацию
                    if (it.error !is PageNotFoundException) {
                        viewState.handleState2(it.type)
                    } else {
                        viewState.handleState2(BasePagingState.Type.NONE)
                    }
                }, {}))

        groupsDisposable.add(RxPagedListBuilder(dsAdm, PAGINATION_PAGE_SIZE)
                .buildObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    viewState.groupListAdmLoaded(it)
                }, {
                    errorHandler.handle(it)
                }))
    }

    fun applySearchQuery(searchQuery:String){
        dsAll.source.applySearchFilter(searchQuery)
        dsSub.source.applySearchFilter(searchQuery)
        dsAdm.source.applySearchFilter(searchQuery)
        refresh()
    }

    fun reload() {
        dsAll.source.reload()
        dsSub.source.reload()
        dsAdm.source.reload()
    }

    fun refresh() {
        unsubscribe()
        groupList()
    }

    fun goToGroupScreen(groupId: String) {
        router.navigateTo(GroupScreen(groupId))
    }

    fun sub(groupId: String) {
        groupsDisposable.add(groupGateway.followGroup(groupId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    groupList()
                    Log.d("123", "Subscribed")
                }, {
                    errorHandler.handle(it)
                }))
    }

    fun unsub(groupId: String) {
        groupsDisposable.add(groupGateway.unfollowGroup(groupId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    groupList()
                    Log.d("123", "UnSubscribed")
                }, {
                    errorHandler.handle(it)
                }))
    }


    override fun onDestroy() {
        super.onDestroy()
        groupsDisposable.clear()
    }

    private fun unsubscribe() {
        groupsDisposable.clear()
    }

}
