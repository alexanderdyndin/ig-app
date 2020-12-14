package com.intergroupapplication.presentation.feature.login.presenter

import moxy.InjectViewState
import com.intergroupapplication.domain.entity.LoginEntity
import com.intergroupapplication.domain.gateway.ImeiGateway
import com.intergroupapplication.domain.gateway.LoginGateway
import com.intergroupapplication.domain.usecase.GetProfileUseCase
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.exstension.handleLoading
import com.intergroupapplication.presentation.feature.ActionApplicationDetailsScreen
import com.intergroupapplication.presentation.feature.login.view.LoginView
import com.intergroupapplication.presentation.feature.recoveryPassword.view.RecoveryPasswordScreen
import com.intergroupapplication.presentation.feature.registration.view.RegistrationScreen
import com.intergroupapplication.presentation.feature.splash.SplashScreen
import com.workable.errorhandler.ErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@InjectViewState
class LoginPresenter @Inject constructor(private val router: Router,
                                         private val loginGateway: LoginGateway,
                                         private val imeiGateway: ImeiGateway,
                                         private val errorHandler: ErrorHandler,
                                         private val getProfileUseCase: GetProfileUseCase)
    : BasePresenter<LoginView>() {

    var navigate: (()-> Unit)? = null;

    fun performLogin(loginEntity: LoginEntity) {
        compositeDisposable.add(loginGateway.performLogin(loginEntity)
                .flatMap { getProfileUseCase.getUserProfile() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .handleLoading(viewState)
                .subscribe({ navigate?.invoke() }) {
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
        router.newRootScreen(RegistrationScreen())
    }

    fun goToSettingsScreen() {
        router.navigateTo(ActionApplicationDetailsScreen())
    }

    fun goToRecoveryPassword() {
        router.navigateTo(RecoveryPasswordScreen())
    }

    private fun goToNavigationScreen() {
        router.newRootScreen(SplashScreen())
    }
}
