package com.intergroupapplication.presentation.feature.registration.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.view.ActionMode
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.coordinatorlayout.widget.CoordinatorLayout
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.RegistrationEntity
import com.intergroupapplication.domain.exception.*
import com.intergroupapplication.presentation.base.BaseActivity
import com.intergroupapplication.presentation.exstension.clicks
import com.intergroupapplication.presentation.exstension.gone
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.registration.presenter.RegistrationPresenter
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.jakewharton.rxbinding2.widget.TextViewAfterTextChangeEvent
import com.mobsandgeeks.saripaar.QuickRule
import com.mobsandgeeks.saripaar.ValidationError
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.Email
import com.mobsandgeeks.saripaar.annotation.NotEmpty
import com.mobsandgeeks.saripaar.annotation.Password
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.exceptions.CompositeException
import kotlinx.android.synthetic.main.activity_registration.*
import kotlinx.android.synthetic.main.auth_loader.*
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class RegistrationActivity : BaseActivity(), RegistrationView, Validator.ValidationListener, ActionMode.Callback {

    companion object {
        private const val DEBOUNCE_TIMEOUT = 300L
        fun getIntent(context: Context?) = Intent(context, RegistrationActivity::class.java)
    }

    @Inject
    @InjectPresenter
    lateinit var presenter: RegistrationPresenter

    @ProvidePresenter
    fun providePresenter(): RegistrationPresenter = presenter

    @Inject
    override lateinit var navigator: SupportAppNavigator

    @NotEmpty(messageResId = R.string.field_should_not_be_empty, trim = true)
    @Email(messageResId = R.string.email_not_valid)
    lateinit var mail: AppCompatEditText

    @NotEmpty(messageResId = R.string.field_should_not_be_empty, trim = true)
    @Password(scheme = Password.Scheme.ALPHA_NUMERIC,
            min = PASSWORD_REQUIRED_LENGTH,
            messageResId = R.string.password_minimum_eight_symbols)
    lateinit var password: AppCompatEditText

    @Inject
    lateinit var validator: Validator

    private lateinit var rxPermission: RxPermissions

    @LayoutRes
    override fun layoutRes() = R.layout.activity_registration

    override fun getSnackBarCoordinator(): CoordinatorLayout = registrationCoordinator

    override fun viewCreated() {
        mail = findViewById(R.id.etMail)
        password = findViewById(R.id.etPassword)
        rxPermission = RxPermissions(this)
        listenInputs()
        RxView.clicks(btnSendEmail)
                .debounce(DEBOUNCE_TIMEOUT, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { validator.validate() }.let(compositeDisposable::add)
        //setErrorHandler()
        btnLogin.clicks().subscribe { presenter.goToLoginScreen() }.also { compositeDisposable.add(it) }
        initValidator()
        initEditText()
    }

    override fun onResume() {
        super.onResume()
        setErrorHandler()
    }

    override fun onPause() {
        errorHandler.clear()
        super.onPause()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun deviceInfoExtracted() {
        presenter.performRegistration(
                RegistrationEntity(etMail.text.toString(),
                        etPassword.text.toString(),
                        etDoubleMail.text.toString(),
                        etDoublePassword.text.toString()))
    }

    override fun onValidationFailed(errors: MutableList<ValidationError>) {
        for (error in errors) {
            val view = error.view
            val message = error.getCollatedErrorMessage(this)
            if (view is AppCompatEditText) {
                when (view.id) {
                    R.id.etMail -> {
                        tvMailError.text = message
                        tvMailError.show()
                    }
                    R.id.etPassword -> {
                        tvErrorPassword.text = message
                        tvErrorPassword.show()
                    }
                    R.id.etDoubleMail -> {
                        tvDoubleMailError.text = message
                        tvDoubleMailError.show()
                    }
                    R.id.etDoublePassword -> {
                        tvDoublePasswdError.text = message
                        tvDoublePasswdError.show()
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

    }

    override fun showLoading(show: Boolean) {
        if (show) {
            progressBar.show()
            btnSendEmail.gone()
        } else {
            progressBar.gone()
            btnSendEmail.show()
        }
    }


    override fun onActionItemClicked(p0: ActionMode?, p1: MenuItem?) = false

    override fun onCreateActionMode(p0: ActionMode?, p1: Menu?) = false

    override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?) = false

    override fun onDestroyActionMode(p0: ActionMode?) = Unit

    private fun listenInputs() {
        Observable.merge(RxTextView.afterTextChangeEvents(mail),
                RxTextView.afterTextChangeEvents(password),
                RxTextView.afterTextChangeEvents(etDoublePassword),
                RxTextView.afterTextChangeEvents(etDoubleMail))
                .subscribe { afterTextChanged ->
                    handleAfterTextChangeEvents(afterTextChanged)
                }.let { compositeDisposable.add(it) }

        etMail.customSelectionActionModeCallback = this
        etDoubleMail.customSelectionActionModeCallback = this
        etPassword.customSelectionActionModeCallback = this
        etDoublePassword.customSelectionActionModeCallback = this
    }

    private fun handleAfterTextChangeEvents(afterTextChanged: TextViewAfterTextChangeEvent) {
        when (afterTextChanged.view().id) {
            R.id.etMail -> {
                tvMailError.gone()
            }
            R.id.etPassword -> {
                tvErrorPassword.gone()
            }
            R.id.etDoublePassword -> {
                tvDoublePasswdError.hide()
            }
            R.id.etDoubleMail -> {
                tvDoubleMailError.gone()
            }
        }
    }

    private fun setErrorHandler() {
        errorHandler.on(CompositeException::class.java) { throwable, _ ->
            run {
                (throwable as? CompositeException)?.exceptions?.forEach { ex ->
                    (ex as? FieldException)?.let {
                        when (it.field) {
                            EMAIL -> {
                                tvMailError.show()
                                tvMailError.text = it.message
                            }
                            PASSWORD -> {
                                tvErrorPassword.show()
                                tvErrorPassword.text = it.message
                            }
                            EMAIL_CONFIRM -> {
                                tvDoubleMailError.show()
                                tvDoubleMailError.text = it.message
                            }
                            PASSWORD_CONFIRM -> {
                                tvDoublePasswdError.show()
                                tvDoublePasswdError.text = it.message
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initValidator() {

        validator.put(etDoubleMail, object : QuickRule<TextView>() {

            override fun getMessage(context: Context?) =
                    getString(R.string.email_not_meeting)


            override fun isValid(view: TextView?): Boolean {
                val mail = view?.text.toString()
                val doubleMail = etMail.text.toString()
                return mail == doubleMail
            }

        })

        validator.put(etDoublePassword, object : QuickRule<TextView>() {

            override fun getMessage(context: Context?) =
                    getString(R.string.password_not_meeting)

            override fun isValid(view: TextView?): Boolean {
                val mail = view?.text.toString()
                val doubleMail = etPassword.text.toString()
                return mail == doubleMail
            }

        })


    }

    private fun initEditText() {
        etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val textEntered = etPassword.text.toString()

                if (textEntered.isNotEmpty() && textEntered.contains(" ")) {
                    etPassword.setText(etPassword.text.toString().replace(" ", ""))
                    etPassword.setSelection(etPassword.text?.length ?: 0)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        })

        etDoublePassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val textEntered = etDoublePassword.text.toString()

                if (textEntered.isNotEmpty() && textEntered.contains(" ")) {
                    etDoublePassword.setText(etDoublePassword.text.toString().replace(" ", ""))
                    etDoublePassword.setSelection(etDoublePassword.text?.length ?: 0)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        })
    }

}
