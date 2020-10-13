package com.intergroupapplication.presentation.feature.confirmationmail.presenter

import moxy.InjectViewState
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.RegistrationEntity
import com.intergroupapplication.domain.gateway.ConfirmationMailGateway
import com.intergroupapplication.domain.gateway.ResendCodeGateway
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.exstension.handleLoading
import com.intergroupapplication.presentation.feature.confirmationmail.view.ConfirmationMailView
import com.intergroupapplication.presentation.feature.createuserprofile.view.CreateUserProfileScreen
import com.workable.errorhandler.ErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@InjectViewState
class ConfirmationMailPresenter @Inject constructor(private val router: Router,
                                                    private val confirmationMailGateway: ConfirmationMailGateway,
                                                    private val errorHandler: ErrorHandler,
                                                    private val entity: String?,
                                                    private val resendCodeGateway: ResendCodeGateway)
    : BasePresenter<ConfirmationMailView>() {

    fun start() {
        entity?.let {
            viewState?.fillData(it)
        }

    }

    fun performRegistration() {
        compositeDisposable.add(resendCodeGateway.resendCode()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { viewState?.showMessage(R.string.send_code_on_email) },
                        { errorHandler.handle(it) }))
    }

    fun confirmMail(confirmCode: String) {
        compositeDisposable.add(
                confirmationMailGateway.confirmMail(confirmCode)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .handleLoading(viewState)
                        .subscribe({
                            router.newRootScreen(CreateUserProfileScreen())
                        }) {
                            viewState.clearViewErrorState()
                            errorHandler.handle(it)
                        })
    }

}