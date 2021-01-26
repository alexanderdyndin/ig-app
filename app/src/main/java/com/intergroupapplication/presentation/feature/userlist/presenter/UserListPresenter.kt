package com.intergroupapplication.presentation.feature.userlist.presenter

import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.domain.gateway.GroupGateway
import com.intergroupapplication.presentation.feature.userlist.view.UserListView
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.exstension.handleLoading
import com.workable.errorhandler.ErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import moxy.InjectViewState
import timber.log.Timber
import javax.inject.Inject

@InjectViewState
class UserListPresenter @Inject constructor(private val errorHandler: ErrorHandler,
                                            private val groupGateway: GroupGateway,
                                            private val sessionStorage: UserSession)
    : BasePresenter<UserListView>() {

    fun groupFollows(groupId: String) {
        compositeDisposable.add(groupGateway.followersGroup(groupId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .handleLoading(viewState)
                .subscribe({
                    //viewState.userListLoaded()
                }, {
                    errorHandler.handle(it)
                }))
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    fun refresh(groupId: String) {
        compositeDisposable.clear()
        groupFollows(groupId)
    }


}