package com.intergroupapplication.presentation.feature.registration.presenter

import moxy.InjectViewState
import com.intergroupapplication.domain.entity.RegistrationEntity
import com.intergroupapplication.domain.gateway.ImeiGateway
import com.intergroupapplication.domain.gateway.RegistrationGateway
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.exstension.handleLoading
import com.intergroupapplication.presentation.feature.registration.view.RegistrationView
import com.workable.errorhandler.ErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

import javax.inject.Inject

@InjectViewState
class RegistrationPresenter @Inject constructor(private val registrationGateway: RegistrationGateway,
                                                private val imeiGateway: ImeiGateway,
                                                private val errorHandler: ErrorHandler)
    : BasePresenter<RegistrationView>() {

    var confirmMail: (email: String) -> Unit = {}

    fun performRegistration(entity: RegistrationEntity) {
        compositeDisposable.add(
                registrationGateway.performRegistration(entity)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .handleLoading(viewState)
                        .subscribe({
                            viewState.confirmMail(entity.email)
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
        //router.newRootScreen(LoginScreen())
    }

    fun goToSettingsScreen() {
        //router.navigateTo(ActionApplicationDetailsScreen())
    }

}
