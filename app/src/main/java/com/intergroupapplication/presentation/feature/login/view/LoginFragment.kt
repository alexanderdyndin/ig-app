package com.intergroupapplication.presentation.feature.login.view

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.text.InputType
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.intergroupapplication.BuildConfig
import com.intergroupapplication.R
import com.intergroupapplication.databinding.FragmentLoginBinding
import com.intergroupapplication.di.qualifier.LoginHandler
import com.intergroupapplication.domain.entity.LoginEntity
import com.intergroupapplication.domain.entity.SocialAuthEntity
import com.intergroupapplication.domain.exception.EMAIL
import com.intergroupapplication.domain.exception.FieldException
import com.intergroupapplication.domain.exception.PASSWORD
import com.intergroupapplication.presentation.base.BaseActivity.Companion.PASSWORD_REQUIRED_LENGTH
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.exstension.clicks
import com.intergroupapplication.presentation.exstension.gone
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.login.presenter.LoginPresenter
import com.intergroupapplication.presentation.listeners.RightDrawableListener
import com.jakewharton.rxbinding3.widget.afterTextChangeEvents
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

class LoginFragment : BaseFragment(), LoginView, Validator.ValidationListener {

    companion object {
        private const val DEBOUNCE_TIMEOUT = 300L
        const val RC_SIGN_IN = 123
    }

    private val viewBinding by viewBinding(FragmentLoginBinding::bind)

    @Inject
    @InjectPresenter
    lateinit var presenter: LoginPresenter

    @ProvidePresenter
    fun providePresenter(): LoginPresenter = presenter

    @SuppressLint("NonConstantResourceId")
    @NotEmpty(messageResId = R.string.field_should_not_be_empty, trim = true)
    @Email(messageResId = R.string.email_not_valid)
    lateinit var mail: AppCompatEditText

    @SuppressLint("NonConstantResourceId")
    @NotEmpty(messageResId = R.string.field_should_not_be_empty, trim = true)
    @Password(
        scheme = Password.Scheme.ALPHA_NUMERIC, min = PASSWORD_REQUIRED_LENGTH,
        messageResId = R.string.password_minimum_eight_symbols
    )
    lateinit var password: AppCompatEditText

    @Inject
    lateinit var validator: Validator

    @Inject
    lateinit var rightDrawableListener: RightDrawableListener

    @Inject
    @LoginHandler
    override lateinit var errorHandler: ErrorHandler

    @LayoutRes
    override fun layoutRes() = R.layout.fragment_login

    override fun getSnackBarCoordinator(): CoordinatorLayout = viewBinding.loginCoordinator

    private lateinit var rxPermission: RxPermissions
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private lateinit var next: AppCompatButton
    private lateinit var registration: AppCompatButton
    private lateinit var recoveryPassword: TextView
    private lateinit var signInButton: SignInButton
    private lateinit var passwordVisibility: TextView
    private lateinit var tvMailError: AppCompatTextView
    private lateinit var tvPasswdError: AppCompatTextView
    private lateinit var progressBar: ProgressBar

    private var passwordVisible = true
    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn
                .getSignedInAccountFromIntent(it.data)
            handleSignInResult(task)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun viewCreated() {
        next = viewBinding.next
        registration = viewBinding.registration
        recoveryPassword = viewBinding.recoveryPassword
        signInButton = viewBinding.signInButton
        passwordVisibility = viewBinding.passwordVisibility
        tvMailError = viewBinding.tvMailError
        tvPasswdError = viewBinding.tvPasswdError
        progressBar = viewBinding.progressBar
        rxPermission = RxPermissions(this)
        mail = viewBinding.etMail
        password = viewBinding.password
        listenInputs()
        next.clicks()
            .debounce(DEBOUNCE_TIMEOUT, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { validator.validate() }
            .also { compositeDisposable.add(it) }
        registration.clicks()
            .subscribe {
                findNavController()
                    .navigate(R.id.action_loginActivity_to_registrationActivity)
            }
            .also { compositeDisposable.add(it) }
        mail.setOnTouchListener(rightDrawableListener)
        recoveryPassword.clicks()
            .subscribe {
                findNavController()
                    .navigate(R.id.action_loginFragment_to_recoveryPasswordFragment)
            }
            .also { compositeDisposable.add(it) }
        signInButton.setOnClickListener {
            startForResult.launch(mGoogleSignInClient.signInIntent)
        }
        visibilityPassword(!passwordVisible)
        passwordVisibility.setOnClickListener {
            visibilityPassword(passwordVisible)
            passwordVisible = !passwordVisible
        }
    }

    private fun visibilityPassword(isVisible: Boolean) {
        val position = password.selectionStart
        if (isVisible) {
            password.inputType =
                InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD or InputType.TYPE_CLASS_TEXT
            passwordVisibility.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.ic_password_invisible,
                0,
                0,
                0
            )
        } else {
            password.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            passwordVisibility.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.ic_password_visible,
                0,
                0,
                0
            )
        }
        password.setSelection(position)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            account.idToken?.let {
                presenter.performLogin(SocialAuthEntity(it))
            }
            Toast.makeText(requireContext(), account.displayName, Toast.LENGTH_SHORT).show()
        } catch (e: ApiException) {
            Timber.w("signInResult:failed code=%s", e.statusCode)
            Toast.makeText(
                requireContext(),
                GoogleSignInStatusCodes.getStatusCodeString(e.statusCode),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestIdToken(BuildConfig.GOOGLE_ID_TOKEN)
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        setErrorHandler()
    }

    override fun deviceInfoExtracted() {
        presenter.performLogin(
            LoginEntity(
                mail.text.toString().trim(),
                password.text.toString().trim()
            )
        )
    }

    override fun login() {
        findNavController().navigate(R.id.action_loginFragment_to_splashFragment)
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
        presenter.extractDeviceInfo()
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
            mail.afterTextChangeEvents(),
            password.afterTextChangeEvents()
        ).subscribe { afterTextChanged ->
            when (afterTextChanged.view.id) {
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
