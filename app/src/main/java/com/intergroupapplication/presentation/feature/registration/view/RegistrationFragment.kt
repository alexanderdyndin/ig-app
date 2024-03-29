package com.intergroupapplication.presentation.feature.registration.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.intergroupapplication.BuildConfig
import com.intergroupapplication.R
import com.intergroupapplication.databinding.FragmentRegistrationBinding
import com.intergroupapplication.di.qualifier.RegistrationHandler
import com.intergroupapplication.domain.entity.RegistrationEntity
import com.intergroupapplication.domain.exception.*
import com.intergroupapplication.presentation.base.BaseActivity.Companion.PASSWORD_REQUIRED_LENGTH
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.exstension.gone
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.registration.presenter.RegistrationPresenter
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.TextViewAfterTextChangeEvent
import com.jakewharton.rxbinding3.widget.afterTextChangeEvents
import com.jakewharton.rxbinding3.widget.textChanges
import com.mobsandgeeks.saripaar.QuickRule
import com.mobsandgeeks.saripaar.ValidationError
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.Email
import com.mobsandgeeks.saripaar.annotation.NotEmpty
import com.mobsandgeeks.saripaar.annotation.Password
import com.tbruyelle.rxpermissions2.RxPermissions
import com.workable.errorhandler.ErrorHandler
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.exceptions.CompositeException
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RegistrationFragment : BaseFragment(), RegistrationView, Validator.ValidationListener,
    ActionMode.Callback {

    companion object {
        private const val DEBOUNCE_TIMEOUT = 300L
    }

    private val viewBinding by viewBinding(FragmentRegistrationBinding::bind)

    @Inject
    @InjectPresenter
    lateinit var presenter: RegistrationPresenter

    @ProvidePresenter
    fun providePresenter(): RegistrationPresenter = presenter


    @NotEmpty(messageResId = R.string.field_should_not_be_empty, trim = true)
    @Email(messageResId = R.string.email_not_valid)
    lateinit var mail: AppCompatEditText

    @NotEmpty(messageResId = R.string.field_should_not_be_empty, trim = true)
    @Password(
        scheme = Password.Scheme.ALPHA_NUMERIC,
        min = PASSWORD_REQUIRED_LENGTH,
        messageResId = R.string.password_minimum_eight_symbols
    )

    @Inject
    lateinit var validator: Validator

    @Inject
    @RegistrationHandler
    override lateinit var errorHandler: ErrorHandler

    private lateinit var rxPermission: RxPermissions
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private var passwordVisible1 = true
    private var passwordVisible2 = true

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            handleSignInResult(it.data)
        }
    }

    @LayoutRes
    override fun layoutRes() = R.layout.fragment_registration

    override fun getSnackBarCoordinator(): CoordinatorLayout = viewBinding.registrationCoordinator

    private lateinit var btnSendEmail: AppCompatButton
    private lateinit var textLogin: AppCompatTextView
    private lateinit var signInButton: SignInButton
    private lateinit var passwordVisibility: TextView
    private lateinit var passwordVisibility2: TextView
    private lateinit var etDoublePassword: AppCompatEditText
    private lateinit var etDoubleMail: AppCompatEditText
    private lateinit var tvDoubleMailError: TextView
    private lateinit var tvDoublePasswdError: TextView
    private lateinit var tvErrorPassword: TextView
    private lateinit var tvMailError: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var password: AppCompatEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestId()
            .requestIdToken(BuildConfig.GOOGLE_ID_TOKEN)
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        setErrorHandler()
    }

    override fun viewCreated() {
        btnSendEmail = viewBinding.btnSendEmail
        textLogin = viewBinding.textLogin
        signInButton = viewBinding.signInButton
        passwordVisibility = viewBinding.passwordVisibility
        passwordVisibility2 = viewBinding.passwordVisibility2
        etDoublePassword = viewBinding.etDoublePassword
        etDoubleMail = viewBinding.etDoubleMail
        tvDoubleMailError = viewBinding.tvDoubleMailError
        tvDoublePasswdError = viewBinding.tvDoublePasswdError
        tvErrorPassword = viewBinding.tvErrorPassword
        tvMailError = viewBinding.tvMailError
        progressBar = viewBinding.progressBar
        mail = viewBinding.etMail
        password = viewBinding.etPassword

        rxPermission = RxPermissions(this)
        listenInputs()
        btnSendEmail.clicks()
            .debounce(DEBOUNCE_TIMEOUT, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { validator.validate() }.also(compositeDisposable::add)

        textLogin.setOnClickListener {
            findNavController()
                .navigate(R.id.action_registrationFragment_to_loginFragment)
        }

        initValidator()
        initEditText()
        visibilityPassword(!passwordVisible1, password, passwordVisibility)
        visibilityPassword(!passwordVisible2, etDoublePassword, passwordVisibility2)
        signInButton.setOnClickListener {
            startForResult.launch(mGoogleSignInClient.signInIntent)
        }
        passwordVisibility.setOnClickListener {
            visibilityPassword(passwordVisible1, password, passwordVisibility)
            passwordVisible1 = !passwordVisible1
        }
        passwordVisibility2.setOnClickListener {
            visibilityPassword(passwordVisible2, etDoublePassword, passwordVisibility2)
            passwordVisible2 = !passwordVisible2
        }
    }

    private fun visibilityPassword(isVisible: Boolean, editText: AppCompatEditText, visibleButton: TextView) {
        val position = editText.selectionStart
        if (isVisible) {
            editText.inputType = InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD or InputType.TYPE_CLASS_TEXT
            visibleButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.ic_password_invisible,
                0,
                0,
                0
            )
        } else {
            editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            visibleButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.ic_password_visible,
                0,
                0,
                0
            )
        }
        editText.setSelection(position)
    }

    private fun handleSignInResult(data: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            Toast.makeText(requireContext(), account.givenName, Toast.LENGTH_SHORT).show()
        } catch (e: ApiException) {
            Timber.w("signInResult:failed code=%s", e.statusCode)
            Toast.makeText(
                requireContext(),
                GoogleSignInStatusCodes.getStatusCodeString(e.statusCode),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun finish() {
        findNavController().popBackStack()
    }

    override fun deviceInfoExtracted() {
        presenter.performRegistration(
            RegistrationEntity(
                mail.text.toString(),
                password.text.toString(),
                etDoubleMail.text.toString(),
                etDoublePassword.text.toString()
            )
        )
    }

    override fun confirmMail(email: String) {
        val bundle = bundleOf("entity" to email)
        view?.findNavController()
            ?.navigate(R.id.action_registrationFragment_to_confirmationMailFragment, bundle)
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
        presenter.extractDeviceInfo()
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
        Observable.merge(
            mail.afterTextChangeEvents(),
            password.afterTextChangeEvents(),
            etDoublePassword.afterTextChangeEvents(),
            etDoubleMail.afterTextChangeEvents()
        )
            .subscribe { afterTextChanged ->
                handleAfterTextChangeEvents(afterTextChanged)
            }.let { compositeDisposable.add(it) }

        mail.customSelectionActionModeCallback = this
        etDoubleMail.customSelectionActionModeCallback = this
        password.customSelectionActionModeCallback = this
        etDoublePassword.customSelectionActionModeCallback = this
    }

    private fun handleAfterTextChangeEvents(afterTextChanged: TextViewAfterTextChangeEvent) {
        when (afterTextChanged.view.id) {
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
                val doubleMail = this@RegistrationFragment.mail.text.toString()
                return mail == doubleMail
            }

        })

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

    private fun initEditText() {
        fun passwordTextChanges(view: AppCompatEditText) {
            view.textChanges()
                .subscribe {
                    val textEntered = it.toString()

                    if (textEntered.isNotEmpty() && textEntered.contains(" ")) {
                        view.setText(it.toString().replace(" ", ""))
                        view.setSelection(password.text?.length ?: 0)
                    }
                }.also(compositeDisposable::add)
        }

        passwordTextChanges(password)
        passwordTextChanges(etDoublePassword)
    }
}
