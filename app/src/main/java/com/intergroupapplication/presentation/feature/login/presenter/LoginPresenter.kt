package com.intergroupapplication.presentation.feature.login.presenter

import com.intergroupapplication.di.qualifier.LoginHandler
import com.intergroupapplication.domain.entity.LoginEntity
import com.intergroupapplication.domain.gateway.ImeiGateway
import com.intergroupapplication.domain.gateway.LoginGateway
import com.intergroupapplication.domain.usecase.UserProfileUseCase
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.exstension.handleLoading
import com.intergroupapplication.presentation.feature.login.view.LoginView
import com.workable.errorhandler.ErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import moxy.InjectViewState
import javax.inject.Inject

@InjectViewState
class LoginPresenter @Inject constructor(
    private val loginGateway: LoginGateway,
    private val imeiGateway: ImeiGateway,
    @LoginHandler
    private val errorHandler: ErrorHandler,
    private val userProfileUseCase: UserProfileUseCase
) : BasePresenter<LoginView>() {


    fun performLogin(loginEntity: LoginEntity) {
        compositeDisposable.add(loginGateway.performLogin(loginEntity)
            .flatMap { userProfileUseCase.getUserProfile() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .handleLoading(viewState)
            .subscribe({ viewState.login() }) {
                errorHandler.handle(it)
            })
    }

    fun extractDeviceInfo() {
        compositeDisposable.addAll(
            imeiGateway.extractDeviceInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ viewState.deviceInfoExtracted() }, { errorHandler.handle(it) })
        )
    }
}
