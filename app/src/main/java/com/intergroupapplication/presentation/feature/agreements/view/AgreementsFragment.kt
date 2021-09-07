package com.intergroupapplication.presentation.feature.agreements.view

import android.view.ViewGroup
import android.widget.*
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.intergroupapplication.R
import com.intergroupapplication.databinding.FragmentAgreements2Binding
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.exstension.clicks
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.agreements.presenter.AgreementsPresenter
import com.jakewharton.rxbinding2.view.RxView.clicks
import io.reactivex.android.schedulers.AndroidSchedulers
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class AgreementsFragment : BaseFragment(), AgreementsView, CompoundButton.OnCheckedChangeListener {

    private companion object {
        const val DEBOUNCE_TIMEOUT = 300L
        const val KEY_PATH = "PATH"
        const val KEY_TITLE = "TITLE"
        const val URL_PRIVACY_POLICY = "https://igsn.net/agreement/1.html"
        const val URL_TERMS_OF_USE = "https://igsn.net/agreement/2.html"
        const val URL_RIGHT_HOLDERS = "https://igsn.net/agreement/3.html"
        const val RES_ID_PRIVACY_POLICY = R.string.privacy_police
        const val RES_ID_TERMS_OF_USE = R.string.terms_of_use
        const val RES_ID_RIGHT_HOLDERS = R.string.rightholders
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
    private lateinit var privacyPolicy: TextView
    private lateinit var userAgreement: TextView
    private lateinit var copyrightAgreement: TextView

    override fun viewCreated() {
        btnNext = viewBinding.btnNext
        progressBar = viewBinding.loader.progressBar
        cbPP = viewBinding.cbPP
        cbRH = viewBinding.cbRH
        cbTOU = viewBinding.cbTOU
        conditionsAgreement = viewBinding.conditionsAgreement
        conditionsCopyrightHolders = viewBinding.conditionsCopyrightHolders
        conditionsPolicy = viewBinding.conditionsPolicy
        privacyPolicy = viewBinding.privacyPolicy
        userAgreement = viewBinding.userAgreement
        copyrightAgreement = viewBinding.copyrightAgreement
        initCheckBox()
        initBtn()
        clicks(btnNext)
            .debounce(DEBOUNCE_TIMEOUT, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { presenter.next() }.let(compositeDisposable::add)
    }

    override fun getSnackBarCoordinator(): ViewGroup = viewBinding.container

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
        val textColor = ContextCompat.getColor(
            requireContext(), if (isChecked)
                android.R.color.white else R.color.manatee
        )
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
            val bundle = bundleOf(
                KEY_PATH to URL_PRIVACY_POLICY,
                KEY_TITLE to RES_ID_PRIVACY_POLICY
            )
            findNavController().navigate(R.id.action_AgreementsFragment2_to_webFragment, bundle)
        }.also { compositeDisposable.add(it) }
        conditionsAgreement.clicks().subscribe {
            val bundle = bundleOf(
                KEY_PATH to URL_TERMS_OF_USE,
                KEY_TITLE to RES_ID_TERMS_OF_USE
            )
            findNavController().navigate(R.id.action_AgreementsFragment2_to_webFragment, bundle)
        }.also { compositeDisposable.add(it) }
        conditionsCopyrightHolders.clicks().subscribe {
            val bundle = bundleOf(
                KEY_PATH to URL_RIGHT_HOLDERS,
                KEY_TITLE to RES_ID_RIGHT_HOLDERS
            )
            findNavController().navigate(R.id.action_AgreementsFragment2_to_webFragment, bundle)
        }.also { compositeDisposable.add(it) }
    }
}
