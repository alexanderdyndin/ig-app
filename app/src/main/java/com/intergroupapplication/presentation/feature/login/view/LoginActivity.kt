package com.intergroupapplication.presentation.feature.login.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.navigation.fragment.findNavController
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.LoginEntity
import com.intergroupapplication.domain.exception.EMAIL
import com.intergroupapplication.domain.exception.FieldException
import com.intergroupapplication.domain.exception.PASSWORD
import com.intergroupapplication.presentation.base.BaseActivity
import com.intergroupapplication.presentation.base.BaseActivity.Companion.PASSWORD_REQUIRED_LENGTH
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.exstension.*
import com.intergroupapplication.presentation.feature.login.presenter.LoginPresenter
import com.intergroupapplication.presentation.listeners.RightDrawableListener
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mobsandgeeks.saripaar.ValidationError
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.Email
import com.mobsandgeeks.saripaar.annotation.NotEmpty
import com.mobsandgeeks.saripaar.annotation.Password
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.exceptions.CompositeException
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.auth_loader.*
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class LoginActivity : BaseFragment(), LoginView, Validator.ValidationListener {

    companion object {
        private const val DEBOUNCE_TIMEOUT = 300L

        fun getIntent(context: Context?) = Intent(context, LoginActivity::class.java)
    }

    @Inject
    @InjectPresenter
    lateinit var presenter: LoginPresenter

    @ProvidePresenter
    fun providePresenter(): LoginPresenter = presenter

    @NotEmpty(messageResId = R.string.field_should_not_be_empty, trim = true)
    @Email(messageResId = R.string.email_not_valid)
    lateinit var mail: AppCompatEditText

    @NotEmpty(messageResId = R.string.field_should_not_be_empty, trim = true)
    @Password(scheme = Password.Scheme.ALPHA_NUMERIC, min = PASSWORD_REQUIRED_LENGTH,
            messageResId = R.string.password_minimum_eight_symbols)
    lateinit var password: AppCompatEditText

    @Inject
    lateinit var validator: Validator

//    @Inject
//    override lateinit var navigator: SupportAppNavigator

    @Inject
    lateinit var rightDrawableListener: RightDrawableListener

    @LayoutRes
    override fun layoutRes() = R.layout.activity_login

    override fun getSnackBarCoordinator(): CoordinatorLayout = loginCoordinator

    private lateinit var rxPermission: RxPermissions

    @SuppressLint("ClickableViewAccessibility")
    override fun viewCreated() {
        presenter.navigate = {findNavController().navigate(R.id.action_loginActivity_to_splashActivity)}
        rxPermission = RxPermissions(this)
        mail = requireView().findViewById(R.id.etMail)
        password = requireView().findViewById(R.id.password)
        listenInputs()
        RxView.clicks(next)
                .debounce(DEBOUNCE_TIMEOUT, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { validator.validate() }
                .also { compositeDisposable.add(it) }
        registration.clicks()
                .subscribe { findNavController().navigate(R.id.action_loginActivity_to_registrationActivity) }
                .also { compositeDisposable.add(it) }
        mail.setOnTouchListener(rightDrawableListener)
        btnRecoveryPassword.clicks()
                .subscribe { presenter.goToRecoveryPassword() }
                .also { compositeDisposable.add(it) }
    }

    override fun onResume() {
        super.onResume()
        initErrorHandler()
        setErrorHandler()
    }

    override fun onPause() {
        errorHandler.clear()
        super.onPause()
    }

    override fun deviceInfoExtracted() {
        presenter.performLogin(LoginEntity(mail.text.toString().trim(), password.text.toString().trim()))
    }

    override fun onValidationFailed(errors: MutableList<ValidationError>) {
        for (error in errors) {
            val view = error.view
            val message = error.getCollatedErrorMessage(requireContext())
            if (view is AppCompatEditText) {
                when (view.id) {
                    R.id.etMail -> {
                        tvMailError.text = message
                        tvMailError.show()
                    }
                    R.id.password -> {
                        tvPasswdError.text = message
                        tvPasswdError.show()
                    }
                }
            }
        }
    }

    override fun onValidationSucceeded() {
        compositeDisposable.add(rxPermission.request(Manifest.permission.READ_PHONE_STATE)
                .subscribe({
                    if (it) {
                        presenter.extractDeviceInfo()
                    } else {
                        dialogDelegate.showDialog(R.layout.dialog_explain_phone_state_permission,
                                mapOf(R.id.permissionOk to {
                                    presenter.goToSettingsScreen()
                                }))
                    }
                }, { Timber.e(it) }))
    }

    override fun clearViewErrorState() {
        registration.isEnabled = true
        next.show()
        progressBar.hide()
    }

    override fun showLoading(show: Boolean) {
        if (show) {
            registration.isEnabled = false
            progressBar.show()
            next.hide()
        } else {
            registration.isEnabled = true
            progressBar.hide()
            next.show()
        }
    }

    private fun listenInputs() {
        Observable.merge(
                RxTextView.afterTextChangeEvents(mail),
                RxTextView.afterTextChangeEvents(password)
        ).subscribe { afterTextChanged ->
            when (afterTextChanged.view().id) {
                R.id.etMail -> {
                    tvMailError.gone()
                }
                R.id.password -> {
                    tvPasswdError.hide()
                }
            }
            next.show()
        }.let(compositeDisposable::add)
    }

    private fun setErrorHandler() {
        errorHandler.on(CompositeException::class.java) { throwable, _ ->
            run {
                (throwable as? CompositeException)?.exceptions?.forEach { ex ->
                    (ex as? FieldException)?.let {
                        if (it.field == EMAIL) {
                            tvMailError.text = it.message
                            tvMailError.show()
                        } else if (it.field == PASSWORD) {
                            tvPasswdError.text = it.message
                            tvPasswdError.show()
                        }
                    }
                }
            }
        }
    }

}
