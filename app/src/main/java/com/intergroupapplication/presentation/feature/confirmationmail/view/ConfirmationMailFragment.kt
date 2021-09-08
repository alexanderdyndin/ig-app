package com.intergroupapplication.presentation.feature.confirmationmail.view

import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatButton
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.intergroupapplication.R
import com.intergroupapplication.databinding.FragmentConfirmationMailBinding
import com.intergroupapplication.di.qualifier.ConfirmationProfileHandler
import com.intergroupapplication.domain.exception.CODE
import com.intergroupapplication.domain.exception.FieldException
import com.intergroupapplication.domain.exception.TOKEN
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.exstension.clicks
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.confirmationmail.presenter.ConfirmationMailPresenter
import com.workable.errorhandler.ErrorHandler
import io.reactivex.exceptions.CompositeException
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import javax.inject.Inject

class ConfirmationMailFragment : BaseFragment(), ConfirmationMailView {

    @Inject
    @InjectPresenter
    lateinit var presenter: ConfirmationMailPresenter

    @Inject
    @ConfirmationProfileHandler
    override lateinit var errorHandler: ErrorHandler

    private val viewBinding by viewBinding(FragmentConfirmationMailBinding::bind)
    private lateinit var btnNext: AppCompatButton
    private lateinit var btnRepeatCode: TextView
    private lateinit var btnChangeEmail: TextView
    private lateinit var confirmation: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var emailConfirmation: TextView
    private lateinit var textConfirmation: TextView
    private lateinit var textConfirmation2: TextView

    @LayoutRes
    override fun layoutRes() = R.layout.fragment_confirmation_mail

    @ProvidePresenter
    fun providePresenter(): ConfirmationMailPresenter = presenter

    override fun getSnackBarCoordinator(): CoordinatorLayout = viewBinding.confirmationCoordinator

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
        findNavController().navigate(R.id.action_confirmationMailFragment_to_createUserProfileFragment)
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
