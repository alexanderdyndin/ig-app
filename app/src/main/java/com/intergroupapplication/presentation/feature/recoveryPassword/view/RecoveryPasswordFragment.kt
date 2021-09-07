package com.intergroupapplication.presentation.feature.recoveryPassword.view

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.intergroupapplication.R
import com.intergroupapplication.databinding.FragmentRecoveryPassword2Binding
import com.intergroupapplication.di.qualifier.RecoveryHandler
import com.intergroupapplication.databinding.FragmentRecoveryPasswordBinding
import com.intergroupapplication.domain.exception.*
import com.intergroupapplication.presentation.base.BaseActivity.Companion.PASSWORD_REQUIRED_LENGTH
import com.intergroupapplication.presentation.base.BaseFragment
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
import com.workable.errorhandler.ErrorHandler
import io.reactivex.Observable
import io.reactivex.exceptions.CompositeException
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import javax.inject.Inject

class RecoveryPasswordFragment : BaseFragment(), RecoveryPasswordView, Validator.ValidationListener,
    View.OnClickListener {

    private val viewBinding by viewBinding(FragmentRecoveryPasswordBinding::bind)

    @Inject
    @InjectPresenter
    lateinit var presenter: RecoveryPasswordPresenter

    @ProvidePresenter
    fun providePresenter(): RecoveryPasswordPresenter = presenter

    @NotEmpty(messageResId = R.string.field_should_not_be_empty, trim = true)
    @Password(
        scheme = Password.Scheme.ALPHA_NUMERIC,
        min = PASSWORD_REQUIRED_LENGTH,
        messageResId = R.string.password_minimum_eight_symbols
    )
    lateinit var password: AppCompatEditText

    @Inject
    lateinit var validator: Validator

    @Inject
    @RecoveryHandler
    override lateinit var errorHandler: ErrorHandler

    override fun getSnackBarCoordinator() = viewBinding.coordinator

    override fun layoutRes() = R.layout.fragment_recovery_password

    private lateinit var btnSave: AppCompatButton
    private lateinit var btnSend: AppCompatButton
    private lateinit var etMail: AppCompatEditText
    private lateinit var etCode: AppCompatEditText
    private lateinit var etDoublePassword: AppCompatEditText
    private lateinit var pbSave: ProgressBar
    private lateinit var pbEmail: ProgressBar
    private lateinit var pbCode: ProgressBar
    private lateinit var tvErrorCode: AppCompatTextView
    private lateinit var tvErrorDoublePass: AppCompatTextView
    private lateinit var tvErrorMail: AppCompatTextView
    private lateinit var tvErrorPass: AppCompatTextView
    private lateinit var tvSendCode: AppCompatTextView
    private lateinit var passwordVisibility: TextView
    private lateinit var passwordVisibility2: TextView

    var passwordVisible = true

    override fun viewCreated() {
        passwordVisibility = viewBinding.passwordVisibility
        passwordVisibility2 = viewBinding.passwordVisibility2
        btnSave = viewBinding.btnSave
        btnSend = viewBinding.btnSend
        etMail = viewBinding.etMail
        etCode = viewBinding.etCode
        etDoublePassword = viewBinding.etDoublePassword
        password = viewBinding.etPassword
        pbSave = viewBinding.pbSave.progressBar
        pbEmail = viewBinding.pbEmail.progressBar
        pbCode = viewBinding.pbCode.progressBar
        tvErrorCode = viewBinding.tvErrorCode
        tvErrorDoublePass = viewBinding.tvErrorDoublePass
        tvErrorMail = viewBinding.tvErrorMail
        tvErrorPass = viewBinding.tvErrorPass
        tvSendCode = viewBinding.tvSendCode

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
        visibilityPassword(!passwordVisible)
        passwordVisibility.setOnClickListener(this)
        passwordVisibility2.setOnClickListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> findNavController().popBackStack()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun showPassword(enable: Boolean) {
        val hintColor = if (enable) requireContext().getColor(R.color.colorAccent2)
        else requireContext().getColor(R.color.colorTextBtnNoActive)
        etDoublePassword.isEnabled = enable
        password.isEnabled = enable
        btnSave.isEnabled = enable
        etDoublePassword.setHintTextColor(hintColor)
        password.setHintTextColor(hintColor)
    }

    override fun successSaveSettings() {
        findNavController().popBackStack()
        Toast.makeText(requireContext(), R.string.save_password, Toast.LENGTH_SHORT).show()
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

    override fun showLoadingCode(load: Boolean) {
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
            val message = error.getCollatedErrorMessage(requireContext())
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
        val password = password.text.toString()
        val doublePassword = etDoublePassword.text.toString()
        presenter.saveSettings(password, doublePassword)
    }

    override fun showSendEmail(visible: Boolean) {
        if (visible) {
            tvSendCode.show()
        } else {
            tvSendCode.hide()
        }
        val hintColor = if (visible) requireContext().getColor(R.color.colorAccent2)
        else requireContext().getColor(R.color.colorTextBtnNoActive)
        with(etCode) {
            setHintTextColor(hintColor)
            text?.clear()
        }
        etCode.isEnabled = visible
    }

    private fun listenInputs() {
        Observable.merge(
            RxTextView.afterTextChangeEvents(etDoublePassword),
            RxTextView.afterTextChangeEvents(password),
            RxTextView.afterTextChangeEvents(etMail),
            RxTextView.afterTextChangeEvents(etCode)
        )
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
        password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val textEntered = password.text.toString()

                if (textEntered.isNotEmpty() && textEntered.contains(" ")) {
                    password.setText(password.text.toString().replace(" ", ""))
                    password.setSelection(password.text?.length ?: 0)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

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

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        })
    }

    override fun onClick(v: View?) {
        visibilityPassword(passwordVisible)
        passwordVisible = !passwordVisible
    }

    private fun visibilityPassword(isVisible: Boolean) {
        if (isVisible) {
            password.inputType =
                InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD or InputType.TYPE_CLASS_TEXT
            passwordVisibility.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.ic_password_visible,
                0,
                0,
                0
            )
            etDoublePassword.inputType =
                InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD or InputType.TYPE_CLASS_TEXT
            passwordVisibility2.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.ic_password_visible,
                0,
                0,
                0
            )
        } else {
            password.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            passwordVisibility.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.ic_password_invisible,
                0,
                0,
                0
            )
            etDoublePassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            passwordVisibility2.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.ic_password_invisible,
                0,
                0,
                0
            )
        }
    }
}
