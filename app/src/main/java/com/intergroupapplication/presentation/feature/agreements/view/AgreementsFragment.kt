package com.intergroupapplication.presentation.feature.agreements.view

import android.Manifest
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.intergroupapplication.R
import com.intergroupapplication.databinding.AuthLoader2Binding
import com.intergroupapplication.databinding.FragmentAgreements2Binding
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.exstension.clicks
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.agreements.presenter.AgreementsPresenter
import com.jakewharton.rxbinding2.view.RxView.clicks
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
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

    private val viewBinding by viewBinding(FragmentAgreements2Binding::bind)


    @Inject
    @InjectPresenter
    lateinit var presenter: AgreementsPresenter

    @ProvidePresenter
    fun providePresenter(): AgreementsPresenter = presenter

    @LayoutRes
    override fun layoutRes() = R.layout.fragment_agreements2

    private lateinit var btnNext: AppCompatButton
    private lateinit var progressBar: ProgressBar
    private lateinit var cbPP: CheckBox
    private lateinit var cbRH: CheckBox
    private lateinit var cbTOU: CheckBox
    private lateinit var conditionsAgreement: LinearLayout
    private lateinit var conditionsCopyrightHolders: LinearLayout
    private lateinit var conditionsPolicy: LinearLayout

    override fun viewCreated() {
        btnNext = viewBinding.btnNext
        progressBar = viewBinding.loader.progressBar
        cbPP = viewBinding.cbPP
        cbRH = viewBinding.cbRH
        cbTOU = viewBinding.cbTOU
        conditionsAgreement = viewBinding.conditionsAgreement
        conditionsCopyrightHolders = viewBinding.conditionsCopyrightHolders
        conditionsPolicy = viewBinding.conditionsPolicy
        initCheckBox()
        initBtn()
        clicks(btnNext)
                .debounce(DEBOUNCE_TIMEOUT, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { next() }.let(compositeDisposable::add)
    }

    override fun getSnackBarCoordinator(): ViewGroup? = viewBinding.container

    override fun toSplash() {
        findNavController().navigate(R.id.action_global_splashActivity)
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
        btnNext.isEnabled = cbRH.isChecked && cbPP.isChecked && cbTOU.isChecked
    }

    private fun initCheckBox() {
        cbRH.setOnCheckedChangeListener(this)
        cbPP.setOnCheckedChangeListener(this)
        cbTOU.setOnCheckedChangeListener(this)
    }

    private fun initBtn() {
        conditionsPolicy.clicks().subscribe {
            val bundle = bundleOf(KEY_PATH to URL_PRIVACY_POLICY, KEY_TITLE to RES_ID_PRIVACY_POLICY)
            findNavController().navigate(R.id.action_AgreementsFragment2_to_webActivity, bundle)
        }.also { compositeDisposable.add(it) }
        conditionsAgreement.clicks().subscribe {
            val bundle = bundleOf(KEY_PATH to URL_TERMS_OF_USE, KEY_TITLE to RES_ID_TERMS_OF_USE)
            findNavController().navigate(R.id.action_AgreementsFragment2_to_webActivity, bundle)
        }.also { compositeDisposable.add(it) }
        conditionsCopyrightHolders.clicks().subscribe {
            val bundle = bundleOf(KEY_PATH to URL_RIGHTHOLDERS, KEY_TITLE to RES_ID_RIGHTHOLDERS)
            findNavController().navigate(R.id.action_AgreementsFragment2_to_webActivity, bundle)
        }.also { compositeDisposable.add(it) }
    }

    private fun next() {
        compositeDisposable.add(
                        RxPermissions(this).request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe({
                            presenter.next()
                        }, { Timber.e(it) }))

    }

}