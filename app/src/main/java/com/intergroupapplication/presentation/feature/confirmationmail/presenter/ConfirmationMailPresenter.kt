package com.intergroupapplication.presentation.feature.confirmationmail.presenter

import com.intergroupapplication.R
import com.intergroupapplication.di.qualifier.ConfirmationProfileHandler
import com.intergroupapplication.domain.gateway.ConfirmationMailGateway
import com.intergroupapplication.domain.gateway.ResendCodeGateway
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.exstension.handleLoading
import com.intergroupapplication.presentation.feature.confirmationmail.view.ConfirmationMailView
import com.workable.errorhandler.ErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import moxy.InjectViewState
import javax.inject.Inject

@InjectViewState
class ConfirmationMailPresenter @Inject constructor(
    private val confirmationMailGateway: ConfirmationMailGateway,
    @ConfirmationProfileHandler
    private val errorHandler: ErrorHandler,
    private val resendCodeGateway: ResendCodeGateway
) : BasePresenter<ConfirmationMailView>() {


    fun start(entity: String?) {
        entity?.let {
            viewState?.fillData(it)
        }

    }

    fun performRegistration() {
        compositeDisposable.add(
            resendCodeGateway.resendCode()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { viewState?.showMessage(R.string.send_code_on_email) },
                    { errorHandler.handle(it) })
        )
    }

    fun confirmMail(confirmCode: String) {
        compositeDisposable.add(
            confirmationMailGateway.confirmMail(confirmCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .handleLoading(viewState)
                .subscribe({
                    viewState.completed()
                }) {
                    viewState.clearViewErrorState()
                    errorHandler.handle(it)
                }
        )
    }
}
