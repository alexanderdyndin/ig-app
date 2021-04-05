package com.intergroupapplication.presentation.feature.confirmationmail.view

import androidx.core.content.ContextCompat
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.navigation.fragment.findNavController
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.intergroupapplication.R
import com.intergroupapplication.domain.exception.*
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.exstension.clicks
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.confirmationmail.presenter.ConfirmationMailPresenter
import com.workable.errorhandler.ErrorHandler
import io.reactivex.exceptions.CompositeException
import kotlinx.android.synthetic.main.fragment_confirmation_mail.*
import kotlinx.android.synthetic.main.auth_loader.*

import javax.inject.Inject
import javax.inject.Named

class ConfirmationMailFragment : BaseFragment(), ConfirmationMailView {

    companion object {
        const val REGISTRATION_ENTITY = "REGISTRATION_ENTITY"
    }

    @Inject
    @InjectPresenter
    lateinit var presenter: ConfirmationMailPresenter

    @LayoutRes
    override fun layoutRes() = R.layout.fragment_confirmation_mail

    @ProvidePresenter
    fun providePresenter(): ConfirmationMailPresenter = presenter

    @Inject
    @Named("mailHandler")
    lateinit var errorHandlerLogin: ErrorHandler

    override fun getSnackBarCoordinator(): CoordinatorLayout = confirmationCoordinator

    override fun viewCreated() {
        presenter.start(arguments?.getString("amount"))
        initErrorHandler(errorHandlerLogin)
        setErrorHandler()

        btnNext.clicks()
                .subscribe { presenter.confirmMail(confirmation.text.toString()) }
                .let { compositeDisposable.add(it) }

        btnChangeEmail.clicks()
                .subscribe { findNavController().navigate(R.id.action_confirmationMailActivity_to_registrationActivity) }
                .also { compositeDisposable.add(it) }

        btnRepeatCode.clicks()
                .subscribe { presenter.performRegistration() }
                .also { compositeDisposable.add(it) }
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            android.R.id.home -> findNavController().navigate(R.id.action_confirmationMailActivity_to_registrationActivity)
//        }
//        return super.onOptionsItemSelected(item)
//    }

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
        val color = ContextCompat.getColor(requireContext(), R.color.cerulean)
        val descriptionMail1 = getString(R.string.description_email_part_1)
        val descriptionMail2 = getString(R.string.description_email_part_2)
        val desc = SpannableString("$descriptionMail1$email$descriptionMail2")
        val start = descriptionMail1.length
        val end = descriptionMail1.length + email.length
        desc.setSpan(ForegroundColorSpan(color), start, end, 0)
        tvWel.text = desc
    }

    override fun completed() {
        findNavController().navigate(R.id.action_confirmationMailActivity_to_createUserProfileActivity)
    }

    override fun showMessage(resId: Int) {
        Toast.makeText(requireContext(), resId, Toast.LENGTH_SHORT).show()
    }

    private fun setErrorHandler() {
        errorHandlerLogin.on(CompositeException::class.java) { throwable, _ ->
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
