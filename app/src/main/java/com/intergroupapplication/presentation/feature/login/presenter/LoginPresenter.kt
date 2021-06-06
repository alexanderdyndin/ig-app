package com.intergroupapplication.presentation.feature.login.presenter

import moxy.InjectViewState
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

import javax.inject.Inject
import javax.inject.Named

@InjectViewState
class LoginPresenter @Inject constructor(private val loginGateway: LoginGateway,
                                         private val imeiGateway: ImeiGateway,
                                         @Named("loginHandler")
                                         private val errorHandler: ErrorHandler,
                                         private val userProfileUseCase: UserProfileUseCase)
    : BasePresenter<LoginView>() {



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
        compositeDisposable.addAll(imeiGateway.extractDeviceInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ viewState.deviceInfoExtracted() }, { errorHandler.handle(it) }))
    }

    fun goToRegistrationScreen() {
        //router.newRootScreen(RegistrationScreen())
    }

    fun goToSettingsScreen() {
        //router.navigateTo(ActionApplicationDetailsScreen())
    }

    fun goToRecoveryPassword() {
        //router.navigateTo(RecoveryPasswordScreen())
    }

    private fun goToNavigationScreen() {
        //router.newRootScreen(SplashScreen())
    }
}
