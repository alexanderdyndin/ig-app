package com.intergroupapplication.presentation.feature.agreements.view

import android.Manifest
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.intergroupapplication.R
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.exstension.clicks
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.agreements.presenter.AgreementsPresenter
import com.jakewharton.rxbinding2.view.RxView.clicks
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.auth_loader.*
import kotlinx.android.synthetic.main.fragment_agreements.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class AgreementsFragment : BaseFragment(), AgreementsView, CompoundButton.OnCheckedChangeListener {

    companion object {
        private const val DEBOUNCE_TIMEOUT = 300L
        private const val KEY_PATH = "PATH"
        private const val KEY_TITLE = "TITLE"
        private const val URL_PRIVACY_POLICY = "https://igsn.net/agreement/1.html"
        private const val URL_TERMS_OF_USE = "https://igsn.net/agreement/2.html"
        private const val URL_RIGHTHOLDERS = "https://igsn.net/agreement/3.html"
        private const val URL_APPODEAL = "https://www.appodeal.com/home/privacy-policy/"

        private const val RES_ID_PRIVACY_POLICY = R.string.privacy_police
        private const val RES_ID_TERMS_OF_USE = R.string.terms_of_use
        private const val RES_ID_RIGHTHOLDERS = R.string.rightholders
        private const val RES_ID_APPODEAL = R.string.appodealpolicy

    }


    @Inject
    @InjectPresenter
    lateinit var presenter: AgreementsPresenter

    @ProvidePresenter
    fun providePresenter(): AgreementsPresenter = presenter

    @LayoutRes
    override fun layoutRes() = R.layout.fragment_agreements

    override fun viewCreated() {
        initCheckBox()
        initBtn()
        clicks(btnNext)
                .debounce(DEBOUNCE_TIMEOUT, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { next() }.let(compositeDisposable::add)
//        ConsentManager.getInstance(this).requestConsentInfoUpdate(
//                BuildConfig.APPODEAL_APP_KEY,
//                object : ConsentInfoUpdateListener {
//                    override fun onConsentInfoUpdated(consent: Consent?) {}
//                    override fun onFailedToUpdateConsentInfo(exception: ConsentManagerException) {}
//                })
//        val consentManager = ConsentManager.getInstance(this)
//        val consent = consentManager.consent
//        val consentZone = consentManager.consentZone
//        val consentStatus = consentManager.consentStatus
//        val iabString = consentManager.iabConsentString
//        val consentFormListener: ConsentFormListener = object : ConsentFormListener {
//            override fun onConsentFormLoaded() {
//                // Consent form was loaded. Now you can display consent form as activity or as dialog
//            }
//            override fun onConsentFormError(error: ConsentManagerException) {
//                // Consent form loading or showing failed. More info can be found in 'error' object
//            }
//            override fun onConsentFormOpened() {
//                // Conset form was shown
//            }
//            override fun onConsentFormClosed(consent: Consent) {
//                // Consent form was closed
//            }
//        }
//
//        val consentForm = ConsentForm.Builder(this as Context)
//                .withListener(consentFormListener)
//                .build()
//        consentForm.load()
//        btnAppodeal.setOnClickListener {
//            Timber.d(consentForm.isLoaded.toString())
//            Timber.d(consentForm.isShowing.toString())
//            consentForm.showAsActivity()
//        }
    }

    override fun getSnackBarCoordinator(): ViewGroup? = container

    override fun toSplash() {
        findNavController().navigate(R.id.action_AgreementsFragment2_to_splashActivity)
    }

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
        val textColor = ContextCompat.getColor(requireContext(), if (isChecked) android.R.color.white else R.color.manatee)
        buttonView?.setTextColor(textColor)
        if (cbRH.isChecked && cbPP.isChecked && cbTOU.isChecked) {
            btnNext.background = ContextCompat.getDrawable(requireContext(), R.drawable.background_button)
            btnNext.isEnabled = true
        } else {
            btnNext.background = ContextCompat.getDrawable(requireContext(), R.drawable.background_button_disable)
            btnNext.isEnabled = false
        }
    }

    private fun initCheckBox() {
        cbRH.setOnCheckedChangeListener(this)
        cbPP.setOnCheckedChangeListener(this)
        cbTOU.setOnCheckedChangeListener(this)
    }

    private fun initBtn() {
        btnPrivacyPolicy.clicks().subscribe {
            val bundle = bundleOf(KEY_PATH to URL_PRIVACY_POLICY, KEY_TITLE to RES_ID_PRIVACY_POLICY)
            findNavController().navigate(R.id.action_AgreementsFragment2_to_webActivity, bundle)
        }.also { compositeDisposable.add(it) }
        btnRightholders.clicks().subscribe {
            val bundle = bundleOf(KEY_PATH to URL_TERMS_OF_USE, KEY_TITLE to RES_ID_TERMS_OF_USE)
            findNavController().navigate(R.id.action_AgreementsFragment2_to_webActivity, bundle)
        }.also { compositeDisposable.add(it) }
        btnTermsOfUse.clicks().subscribe {
            val bundle = bundleOf(KEY_PATH to URL_RIGHTHOLDERS, KEY_TITLE to RES_ID_RIGHTHOLDERS)
            findNavController().navigate(R.id.action_AgreementsFragment2_to_webActivity, bundle)
        }.also { compositeDisposable.add(it) }
    }

    private fun next() {
//        Appodeal.requestAndroidMPermissions(requireActivity(), object : PermissionsHelper.AppodealPermissionCallbacks {
//            override fun writeExternalStorageResponse(result: Int) {
//                if (result == PackageManager.PERMISSION_GRANTED) {
//                    //showToast("WRITE_EXTERNAL_STORAGE permission was granted")
//                } else {
//                    //showToast("WRITE_EXTERNAL_STORAGE permission was NOT granted")
//                }
//            }
//
//            override fun accessCoarseLocationResponse(result: Int) {
//                if (result == PackageManager.PERMISSION_GRANTED) {
//                    //showToast("ACCESS_COARSE_LOCATION permission was granted")
//                } else {
//                    //showToast("ACCESS_COARSE_LOCATION permission was NOT granted")
//                }
//            }
//        })
        compositeDisposable.add(
                        RxPermissions(this).request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe({
//                            if (it) {
//                                presenter.next()
//                            } else {
//                                dialogDelegate.showDialog(R.layout.dialog_explain_phone_state_permission,
//                                        mapOf(R.id.permissionOk to {
//                                            presenter.goToSettingsScreen()
//                                        }))
//                            }
                            presenter.next()
                        }, { Timber.e(it) }))

    }

}