package com.intergroupapplication.presentation.feature.agreements.presenter

import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.domain.entity.TermsEntity
import com.intergroupapplication.domain.gateway.ImeiGateway
import com.intergroupapplication.domain.gateway.PermissionAuthorizeGateway
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.exstension.handleLoading
import com.intergroupapplication.presentation.feature.agreements.view.AgreementsView
import com.workable.errorhandler.ErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import moxy.InjectViewState
import javax.inject.Inject

@InjectViewState
class AgreementsPresenter @Inject constructor(
    private val errorHandler: ErrorHandler,
    private val imeiGateway: ImeiGateway, private val userSession: UserSession,
    private val permissionGateway: PermissionAuthorizeGateway
) : BasePresenter<AgreementsView>() {

    fun next() {
        compositeDisposable.add(
            imeiGateway.extractDeviceInfo()
                .subscribeOn(Schedulers.io())
                .andThen(permissionGateway.isBlocked())
                .observeOn(AndroidSchedulers.mainThread())
                .handleLoading(viewState)
                .subscribe({
                    userSession.acceptTerms = TermsEntity(true)
                    viewState.toSplash()
                }, { errorHandler.handle(it) })
        )
    }

}