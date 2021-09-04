package com.intergroupapplication.presentation.feature.confirmationmail.view

import androidx.core.content.ContextCompat
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatButton
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.intergroupapplication.R
import com.intergroupapplication.databinding.FragmentConfirmationMail2Binding
import com.intergroupapplication.domain.exception.*
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.exstension.clicks
import com.intergroupapplication.presentation.exstension.gone
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.confirmationmail.presenter.ConfirmationMailPresenter
import com.workable.errorhandler.ErrorHandler
import io.reactivex.exceptions.CompositeException

import javax.inject.Inject
import javax.inject.Named

class ConfirmationMailFragment : BaseFragment(), ConfirmationMailView {

    companion object {
        const val REGISTRATION_ENTITY = "REGISTRATION_ENTITY"
    }

    private val viewBinding by viewBinding(FragmentConfirmationMail2Binding::bind)

    @Inject
    @InjectPresenter
    lateinit var presenter: ConfirmationMailPresenter

    @LayoutRes
    override fun layoutRes() = R.layout.fragment_confirmation_mail2

    @ProvidePresenter
    fun providePresenter(): ConfirmationMailPresenter = presenter

    override fun getSnackBarCoordinator(): CoordinatorLayout = viewBinding.confirmationCoordinator

    @Inject
    @Named("ConfirmationProfileHandler")
    override lateinit var errorHandler: ErrorHandler

    private lateinit var btnNext: AppCompatButton
    private lateinit var btnRepeatCode: TextView
    private lateinit var btnChangeEmail: TextView
    private lateinit var confirmation: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var emailConfirmation: TextView
    private lateinit var textConfirmation: TextView
    private lateinit var textConfirmation2: TextView

    override fun viewCreated() {
        btnNext = viewBinding.btnNext
        btnRepeatCode = viewBinding.btnRepeatCode
        btnChangeEmail = viewBinding.btnChangeEmail
        confirmation = viewBinding.confirmation
        progressBar = viewBinding.loader.progressBar
        emailConfirmation = viewBinding.emailConfirmation
        textConfirmation = viewBinding.textConfirmation1
        textConfirmation2 = viewBinding.textConfirmation2

        presenter.start(arguments?.getString("entity"))
        setErrorHandler()

        btnNext.clicks()
                .subscribe { presenter.confirmMail(confirmation.text.toString()) }
                .let { compositeDisposable.add(it) }

        btnRepeatCode.clicks()
                .subscribe { logOut() }
                .also { compositeDisposable.add(it) }

        btnChangeEmail.clicks()
                .subscribe { presenter.performRegistration() }
                .also { compositeDisposable.add(it) }
    }


    override fun clearViewErrorState() {
        confirmation.text?.clear()
    }

    override fun showLoading(show: Boolean) {
        if (show) {
            progressBar.show()
            btnNext.hide()
        } else {
            btnNext.show()
            progressBar.hide()
        }
    }

    override fun fillData(email: String) {
        if (email.isNotEmpty())
            emailConfirmation.text = email
        else {
            textConfirmation.hide()
            textConfirmation.hide()
            emailConfirmation.hide()
        }
    }

    override fun completed() {
        findNavController().navigate(R.id.action_confirmationMailActivity_to_createUserProfileActivity)
    }

    override fun showMessage(resId: Int) {
        Toast.makeText(requireContext(), resId, Toast.LENGTH_SHORT).show()
    }

    private fun setErrorHandler() {
        errorHandler.on(CompositeException::class.java) { throwable, _ ->
            run {
                (throwable as? CompositeException)?.exceptions?.forEach { ex ->
                    (ex as? FieldException)?.let {
                        when (it.field) {
                            TOKEN -> showErrorMessage(it.message.orEmpty())
                            CODE -> showErrorMessage(it.message.orEmpty())
                        }
                    }
                }
            }
        }
    }

}
