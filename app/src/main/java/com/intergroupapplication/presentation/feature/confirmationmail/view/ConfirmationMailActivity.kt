package com.intergroupapplication.presentation.feature.confirmationmail.view

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.RegistrationEntity
import com.intergroupapplication.domain.exception.*
import com.intergroupapplication.presentation.base.BaseActivity
import com.intergroupapplication.presentation.exstension.clicks
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.confirmationmail.presenter.ConfirmationMailPresenter
import io.reactivex.exceptions.CompositeException
import kotlinx.android.synthetic.main.activity_confirmation_mail.*
import kotlinx.android.synthetic.main.activity_registration.*
import kotlinx.android.synthetic.main.auth_loader.*
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import javax.inject.Inject

class ConfirmationMailActivity : BaseActivity(), ConfirmationMailView {

    companion object {
        const val REGISTRATION_ENTITY = "REGISTRATION_ENTITY"

        fun getIntent(context: Context?, entity: String) =
                Intent(context, ConfirmationMailActivity::class.java)
                        .apply {
                            putExtra(REGISTRATION_ENTITY, entity)
                        }
    }

    @Inject
    @InjectPresenter
    lateinit var presenter: ConfirmationMailPresenter

    @LayoutRes
    override fun layoutRes() = R.layout.activity_confirmation_mail

    @ProvidePresenter
    fun providePresenter(): ConfirmationMailPresenter = presenter

    @Inject
    override lateinit var navigator: SupportAppNavigator

    override fun getSnackBarCoordinator(): CoordinatorLayout = confirmationCoordinator

    override fun viewCreated() {
        presenter.start()

        setSupportActionBar(tollbar)
        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = ""
        }

        setErrorHandler()

        btnNext.clicks()
                .subscribe { presenter.confirmMail(confirmation.text.toString()) }
                .let { compositeDisposable.add(it) }

        btnChangeEmail.clicks()
                .subscribe { finish() }
                .also { compositeDisposable.add(it) }

        btnRepeatCode.clicks()
                .subscribe { presenter.performRegistration() }
                .also { compositeDisposable.add(it) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
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
        val color = ContextCompat.getColor(this, R.color.cerulean)
        val descriptionMail1 = getString(R.string.description_email_part_1)
        val descriptionMail2 = getString(R.string.description_email_part_2)
        val desc = SpannableString("$descriptionMail1$email$descriptionMail2")
        val start = descriptionMail1.length
        val end = descriptionMail1.length + email.length
        desc.setSpan(ForegroundColorSpan(color), start, end, 0)
        tvWel.text = desc
    }

    override fun showMessage(resId: Int) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
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
