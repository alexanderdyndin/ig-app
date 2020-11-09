package com.intergroupapplication.presentation.feature.recoveryPassword.view

import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.intergroupapplication.R
import com.intergroupapplication.domain.exception.*
import com.intergroupapplication.presentation.base.BaseActivity
import com.intergroupapplication.presentation.exstension.clicks
import com.intergroupapplication.presentation.exstension.gone
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.recoveryPassword.presenter.RecoveryPasswordPresenter
import com.jakewharton.rxbinding2.widget.RxTextView
import com.jakewharton.rxbinding2.widget.TextViewAfterTextChangeEvent
import com.mobsandgeeks.saripaar.QuickRule
import com.mobsandgeeks.saripaar.ValidationError
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.NotEmpty
import com.mobsandgeeks.saripaar.annotation.Password
import io.reactivex.Observable
import io.reactivex.exceptions.CompositeException
import kotlinx.android.synthetic.main.activity_recovery_password.*
import kotlinx.android.synthetic.main.activity_recovery_password.etDoublePassword
import kotlinx.android.synthetic.main.activity_recovery_password.etMail
import kotlinx.android.synthetic.main.activity_recovery_password.etPassword
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import javax.inject.Inject

class RecoveryPasswordActivity : BaseActivity(), RecoveryPasswordView, Validator.ValidationListener {

    companion object {
        fun getIntent(context: Context?): Intent =
                Intent(context, RecoveryPasswordActivity::class.java)
    }

    @Inject
    override lateinit var navigator: SupportAppNavigator

    @Inject
    @InjectPresenter
    lateinit var presenter: RecoveryPasswordPresenter

    @ProvidePresenter
    fun providePresenter(): RecoveryPasswordPresenter = presenter

    @NotEmpty(messageResId = R.string.field_should_not_be_empty, trim = true)
    @Password(scheme = Password.Scheme.ALPHA_NUMERIC,
            min = PASSWORD_REQUIRED_LENGTH,
            messageResId = R.string.password_minimum_eight_symbols)
    lateinit var password: AppCompatEditText

    @Inject
    lateinit var validator: Validator

    override fun layoutRes() = R.layout.activity_recovery_password

    override fun getSnackBarCoordinator() = coordinator

    override fun viewCreated() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setTitle(R.string.settings_password)
        }
        password = findViewById(R.id.etPassword)

        listenInputs()
        initValidator()
        setErrorHandler()
        initEditText()

        btnSave.clicks()
                .subscribe { validator.validate() }
                .also { compositeDisposable.add(it) }
        btnSend.clicks()
                .subscribe {
                    val email = etMail.text.toString()
                    presenter.sendEmail(email)
                }.also { compositeDisposable.add(it) }
        RxTextView.textChangeEvents(etCode)
                .subscribe {
                    val code = etCode.text.toString()
                    presenter.sendCode(code)
                }
                .also { compositeDisposable.add(it) }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun showPassword(enable: Boolean) {
        val drawableIdTv = if (enable) R.drawable.background_edit_text else R.drawable.background_edit_text_disable
        val drawableIdBtn = if (enable) R.drawable.background_button else R.drawable.background_button_disable
        etDoublePassword.isEnabled = enable
        etPassword.isEnabled = enable
        btnSave.isEnabled = enable
        etDoublePassword.setBackgroundResource(drawableIdTv)
        etPassword.setBackgroundResource(drawableIdTv)
        btnSave.setBackgroundResource(drawableIdBtn)
    }

    override fun successSaveSetings() {
        finish()
        Toast.makeText(this, R.string.save_password, Toast.LENGTH_SHORT).show()
    }

    override fun showLoading(show: Boolean) {
        if (show) {
            btnSave.hide()
            pbSave.show()
        } else {
            btnSave.show()
            pbSave.hide()
        }
    }

    override fun showLoadingSendEmail(value: Boolean) {
        if (value) {
            btnSend.hide()
            pbEmail.show()
        } else {
            btnSend.show()
            pbEmail.hide()
        }
    }

    override fun showLodingCode(load: Boolean) {
        if (load) {
            etCode.hide()
            pbCode.show()
        } else {
            etCode.show()
            pbCode.hide()
        }
    }

    override fun clearViewErrorState() {}

    override fun onValidationFailed(errors: MutableList<ValidationError>) {
        for (error in errors) {
            val view = error.view
            val message = error.getCollatedErrorMessage(this)
            if (view is AppCompatEditText) {
                when (view.id) {
                    R.id.etPassword -> {
                        tvErrorPass.show()
                        tvErrorPass.text = message
                    }
                    R.id.etDoublePassword -> {
                        tvErrorDoublePass.show()
                        tvErrorDoublePass.text = message
                    }
                }
            }
        }
    }

    override fun onValidationSucceeded() {
        val password = etPassword.text.toString()
        val doublePassword = etDoublePassword.text.toString()
        presenter.saveSettings(password, doublePassword)
    }

    override fun showSendEmail(visible: Boolean) {
        if (visible) {
            tvSendCode.show()
        } else {
            tvSendCode.hide()
        }
        val drawableIdTv = if (visible) R.drawable.background_edit_text else R.drawable.background_edit_text_disable
        with(etCode) {
            setBackgroundResource(drawableIdTv)
            text?.clear()
        }
        etCode.isEnabled = visible
    }

    private fun listenInputs() {
        Observable.merge(RxTextView.afterTextChangeEvents(etDoublePassword),
                RxTextView.afterTextChangeEvents(password),
                RxTextView.afterTextChangeEvents(etMail),
                RxTextView.afterTextChangeEvents(etCode))
                .subscribe { afterTextChanged ->
                    handleAfterTextChangeEvents(afterTextChanged)
                }.let { compositeDisposable.add(it) }
    }

    private fun handleAfterTextChangeEvents(afterTextChanged: TextViewAfterTextChangeEvent) {
        when (afterTextChanged.view().id) {
            R.id.etPassword -> tvErrorPass.gone()
            R.id.etDoublePassword -> tvErrorDoublePass.gone()
            R.id.etMail -> tvErrorMail.gone()
            R.id.etCode -> tvErrorCode.gone()
        }
    }

    private fun initValidator() {

        validator.put(etDoublePassword, object : QuickRule<TextView>() {

            override fun getMessage(context: Context?) =
                    getString(R.string.password_not_meeting)

            override fun isValid(view: TextView?): Boolean {
                val mail = view?.text.toString()
                val doubleMail = password.text.toString()
                return mail == doubleMail
            }
        })

    }

    private fun setErrorHandler() {
        errorHandler.on(CompositeException::class.java) { throwable, _ ->
            run {
                (throwable as? CompositeException)?.exceptions?.forEach { ex ->
                    (ex as? FieldException)?.let {
                        when (it.field) {
                            EMAIL -> {
                                tvErrorMail.text = it.message
                                tvErrorMail.show()
                            }
                            PASSWORD -> {
                                tvErrorPass.text = it.message
                                tvErrorPass.show()
                            }
                            CODE -> {
                                tvErrorCode.text = it.message
                                tvErrorCode.show()
                            }
                            PASSWORD_CONFIRM -> {
                                tvErrorDoublePass.text = it.message
                                tvErrorDoublePass.show()
                            }
                        }
                    }
                }
            }
        }
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