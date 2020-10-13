package com.intergroupapplication.presentation.feature.registration.presenter

import moxy.InjectViewState
import com.intergroupapplication.domain.entity.RegistrationEntity
import com.intergroupapplication.domain.gateway.ImeiGateway
import com.intergroupapplication.domain.gateway.RegistrationGateway
import com.intergroupapplication.presentation.Screens
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.exstension.handleLoading
import com.intergroupapplication.presentation.feature.ActionApplicationDetailsScreen
import com.intergroupapplication.presentation.feature.confirmationmail.view.ConfirmationMailScreen
import com.intergroupapplication.presentation.feature.login.view.LoginScreen
import com.intergroupapplication.presentation.feature.registration.view.RegistrationView
import com.workable.errorhandler.ErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@InjectViewState
class RegistrationPresenter @Inject constructor(private val router: Router,
                                                private val registrationGateway: RegistrationGateway,
                                                private val imeiGateway: ImeiGateway,
                                                private val errorHandler: ErrorHandler)
    : BasePresenter<RegistrationView>() {

    fun performRegistration(entity: RegistrationEntity) {
        compositeDisposable.add(
                registrationGateway.performRegistration(entity)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .handleLoading(viewState)
                        .subscribe({
                            router.navigateTo(ConfirmationMailScreen(entity.email))
                        }) {
                            errorHandler.always { _, _ -> viewState.clearViewErrorState() }
                                    .handle(it)
                        })
    }

    fun extractDeviceInfo() {
        compositeDisposable.addAll(imeiGateway.extractDeviceInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ viewState.deviceInfoExtracted() }, {
                    errorHandler.handle(it)
                }))
    }

    fun goToLoginScreen() {
        router.newRootScreen(LoginScreen())
    }

    fun goToSettingsScreen() {
        router.navigateTo(ActionApplicationDetailsScreen())
    }

}
