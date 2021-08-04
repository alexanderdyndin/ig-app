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
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.intergroupapplication.R
import com.intergroupapplication.databinding.FragmentConfirmationMailBinding
import com.intergroupapplication.domain.exception.*
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.exstension.clicks
import com.intergroupapplication.presentation.exstension.gone
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.confirmationmail.presenter.ConfirmationMailPresenter
import io.reactivex.exceptions.CompositeException

import javax.inject.Inject

class ConfirmationMailFragment : BaseFragment(), ConfirmationMailView {

    companion object {
        const val REGISTRATION_ENTITY = "REGISTRATION_ENTITY"
    }

    private val viewBinding by viewBinding(FragmentConfirmationMailBinding::bind)

    @Inject
    @InjectPresenter
    lateinit var presenter: ConfirmationMailPresenter

    @LayoutRes
    override fun layoutRes() = R.layout.fragment_confirmation_mail

    @ProvidePresenter
    fun providePresenter(): ConfirmationMailPresenter = presenter

    override fun getSnackBarCoordinator(): CoordinatorLayout = viewBinding.confirmationCoordinator

    private lateinit var btnNext: AppCompatButton
    private lateinit var btnRepeatCode: TextView
    private lateinit var btnChangeEmail: TextView
    private lateinit var confirmation: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var emailConfirmation: TextView

    override fun viewCreated() {
        btnNext = viewBinding.btnNext
        btnRepeatCode = viewBinding.btnRepeatCode
        btnChangeEmail = viewBinding.btnChangeEmail
        confirmation = viewBinding.confirmation
        progressBar = viewBinding.loader.progressBar
        emailConfirmation = viewBinding.emailConfirmation

        presenter.start(arguments?.getString("entity"))
        setErrorHandler()

        btnNext.clicks()
                .subscribe { presenter.confirmMail(confirmation.text.toString()) }
                .let { compositeDisposable.add(it) }

        btnRepeatCode.clicks()
                .subscribe { findNavController().navigate(R.id.action_confirmationMailFragment_to_registrationFragment) }
                .also { compositeDisposable.add(it) }

        btnChangeEmail.clicks()
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
//        val color = ContextCompat.getColor(requireContext(), R.color.cerulean)
//        val descriptionMail1 = getString(R.string.description_email_part_1)
//        val descriptionMail2 = getString(R.string.description_email_part_2)
//        val desc = SpannableString("$descriptionMail1$email$descriptionMail2")
//        val start = descriptionMail1.length
//        val end = descriptionMail1.length + email.length
//        desc.setSpan(ForegroundColorSpan(color), start, end, 0)
//        tvWel.text = desc
        if (email.isNotEmpty())
            emailConfirmation.text = email
        else
            emailConfirmation.gone()
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
