package com.intergroupapplication.presentation.feature.registration.presenter

import com.intergroupapplication.di.qualifier.RegistrationHandler
import com.intergroupapplication.domain.entity.RegistrationEntity
import com.intergroupapplication.domain.gateway.ImeiGateway
import com.intergroupapplication.domain.gateway.RegistrationGateway
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.exstension.handleLoading
import com.intergroupapplication.presentation.feature.registration.view.RegistrationView
import com.workable.errorhandler.ErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import moxy.InjectViewState
import javax.inject.Inject

@InjectViewState
class RegistrationPresenter @Inject constructor(private val registrationGateway: RegistrationGateway,
                                                private val imeiGateway: ImeiGateway,
                                                @RegistrationHandler
                                                private val errorHandler: ErrorHandler)
    : BasePresenter<RegistrationView>() {

    fun performRegistration(entity: RegistrationEntity) {
        compositeDisposable.add(
                registrationGateway.performRegistration(entity)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .handleLoading(viewState)
                        .subscribe({
                            viewState.confirmMail(entity.email)
                        }) {
                            errorHandler.handle(it)
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

}
