package com.intergroupapplication.presentation.feature.agreements.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.annotation.LayoutRes
import com.appodeal.ads.Appodeal
import com.appodeal.ads.utils.PermissionsHelper
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.intergroupapplication.R
import com.intergroupapplication.presentation.base.BaseActivity
import com.intergroupapplication.presentation.exstension.clicks
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.agreements.presenter.AgreementsPresenter
import com.jakewharton.rxbinding2.view.RxView.clicks
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_agreements.*
import kotlinx.android.synthetic.main.auth_loader.*
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AgreementsActivity : BaseActivity(), AgreementsView, CompoundButton.OnCheckedChangeListener {

    companion object {
        private const val DEBOUNCE_TIMEOUT = 300L

        fun getIntent(context: Context) = Intent(context, AgreementsActivity::class.java)
    }

    @Inject
    override lateinit var navigator: SupportAppNavigator

    @Inject
    @InjectPresenter
    lateinit var presenter: AgreementsPresenter

    @ProvidePresenter
    fun providePresenter(): AgreementsPresenter = presenter

    @LayoutRes
    override fun layoutRes() = R.layout.activity_agreements

    override fun viewCreated() {
        initCheckBox()
        initBtn()
        clicks(btnNext)
                .debounce(DEBOUNCE_TIMEOUT, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { next() }.let(compositeDisposable::add)
    }

    override fun getSnackBarCoordinator(): ViewGroup? = container

    override fun showLoading(show: Boolean) {
        if (show) {
            progressBar.show()
            btnNext.hide()
        } else {
            progressBar.hide()
            btnNext.show()
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        val textColor = ContextCompat.getColor(this, if (isChecked) android.R.color.white else R.color.manatee)
        buttonView?.setTextColor(textColor)
        if (cbRH.isChecked && cbPP.isChecked && cbTOU.isChecked && cbAp.isChecked) {
            btnNext.background = ContextCompat.getDrawable(this, R.drawable.background_button)
            btnNext.isEnabled = true
        } else {
            btnNext.background = ContextCompat.getDrawable(this, R.drawable.background_button_disable)
            btnNext.isEnabled = false
        }
    }

    private fun initCheckBox() {
        cbRH.setOnCheckedChangeListener(this)
        cbPP.setOnCheckedChangeListener(this)
        cbTOU.setOnCheckedChangeListener(this)
        cbAp.setOnCheckedChangeListener(this)
    }

    private fun initBtn() {
        btnPrivacyPolicy.clicks().subscribe { presenter.openPrivacyPolicy() }.also { compositeDisposable.add(it) }
        btnRightholders.clicks().subscribe { presenter.openRightholders() }.also { compositeDisposable.add(it) }
        btnTermsOfUse.clicks().subscribe { presenter.openTermsOfUse() }.also { compositeDisposable.add(it) }
        btnAppodeal.clicks().subscribe { presenter.openAppodealPolicy() }.also { compositeDisposable.add(it) }
    }

    private fun next() {
        compositeDisposable.add(RxPermissions(this).request(Manifest.permission.READ_PHONE_STATE)
                .subscribe({
                    Appodeal.requestAndroidMPermissions(this, object : PermissionsHelper.AppodealPermissionCallbacks {
                        override fun writeExternalStorageResponse(result: Int) {
                            if (result == PackageManager.PERMISSION_GRANTED) {
                                showToast("WRITE_EXTERNAL_STORAGE permission was granted")
                            } else {
                                showToast("WRITE_EXTERNAL_STORAGE permission was NOT granted")
                            }
                        }

                        override fun accessCoarseLocationResponse(result: Int) {
                            if (result == PackageManager.PERMISSION_GRANTED) {
                                showToast("ACCESS_COARSE_LOCATION permission was granted")
                            } else {
                                showToast("ACCESS_COARSE_LOCATION permission was NOT granted")
                            }
                        }
                    })
                    if (it) {
                        presenter.next()
                    } else {
                        dialogDelegate.showDialog(R.layout.dialog_explain_phone_state_permission,
                                mapOf(R.id.permissionOk to {
                                    presenter.goToSettingsScreen()
                                }))
                    }
                }, { Timber.e(it) }))

    }

}