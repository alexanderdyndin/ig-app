package com.intergroupapplication.presentation.feature.recoveryPassword.presenter

import com.intergroupapplication.data.model.CodeModel
import com.intergroupapplication.data.model.EmailModel
import com.intergroupapplication.data.model.NewPasswordModel
import com.intergroupapplication.di.module.NetworkModule.Companion.TOKEN_PREFIX
import com.intergroupapplication.di.qualifier.RecoveryHandler
import com.intergroupapplication.domain.gateway.ResetPasswordGetaway
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.exstension.handleLoading
import com.intergroupapplication.presentation.feature.recoveryPassword.view.RecoveryPasswordView
import com.workable.errorhandler.ErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import moxy.InjectViewState
import javax.inject.Inject

@InjectViewState
class RecoveryPasswordPresenter @Inject constructor(private val resetPasswordGetaway: ResetPasswordGetaway,
                                                    @RecoveryHandler
                                                    private val errorHandler: ErrorHandler)
    : BasePresenter<RecoveryPasswordView>() {

    private var email: String? = null
    private var token: String? = null

    fun sendEmail(email: String) {
        this.email = email
        resetPasswordGetaway.resetPassword(EmailModel(email))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { viewState?.showLoadingSendEmail(true) }
                .doFinally { viewState?.showLoadingSendEmail(false) }
                .subscribe({
                    viewState?.showSendEmail(true)
                    viewState?.showPassword(false)
                }, {
                    viewState?.showSendEmail(false)
                    errorHandler.handle(it)
                })
                .also { compositeDisposable.add(it) }
    }

    fun sendCode(code: String) {
        if (code.length != 4)
            return
        val codeModel = CodeModel(email.orEmpty(), code)
        resetPasswordGetaway.resetPasswordCode(codeModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            token = "$TOKEN_PREFIX ${it.token}"
                            viewState?.showPassword(true)
                        },
                        {
                            viewState?.showPassword(false)
                            errorHandler.handle(it)
                        })
                .also { compositeDisposable.add(it) }
    }

    fun saveSettings(password: String, passwordConfirm: String) {
        val newPassword = NewPasswordModel(token.orEmpty(), password, passwordConfirm)
        resetPasswordGetaway.newPassword(newPassword)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .handleLoading(viewState)
                .subscribe(
                        { viewState?.successSaveSetings() },
                        { errorHandler.handle(it) })
                .also { compositeDisposable.add(it) }
    }

}