package com.intergroupapplication.presentation.feature.agreements.view

import android.view.ViewGroup
import android.widget.*
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.intergroupapplication.R
import com.intergroupapplication.databinding.FragmentAgreementsBinding
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.exstension.clicks
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.factory.ViewModelFactory
import com.intergroupapplication.presentation.feature.agreement.view.AgreementFragment.Companion.RES_ID
import com.intergroupapplication.presentation.feature.agreements.viewmodel.AgreementsViewModel
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class AgreementsFragment : BaseFragment(), CompoundButton.OnCheckedChangeListener {

    companion object {
        private const val DEBOUNCE_TIMEOUT = 200L
        const val RES_ID_PRIVACY_POLICY = R.string.privacy_police
        const val RES_ID_TERMS_OF_USE = R.string.terms_of_use
        const val RES_ID_RIGHTHOLDERS = R.string.rightholders
    }

    private val viewBinding by viewBinding(FragmentAgreementsBinding::bind)

    @Inject
    lateinit var modelFactory: ViewModelFactory

    private val viewModel: AgreementsViewModel by viewModels { modelFactory }

    @LayoutRes
    override fun layoutRes() = R.layout.fragment_agreements

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
        initObservers()
        initCheckBox()
        initBtn()
        btnNext.clicks()
            .debounce(DEBOUNCE_TIMEOUT, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { viewModel.next() }.let(compositeDisposable::add)
    }

    override fun getSnackBarCoordinator(): ViewGroup = viewBinding.container

    private fun toSplash() {
        findNavController().navigate(R.id.action_global_splashActivity)
    }

    private fun showLoading(show: Boolean) {
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
        privacyPolicy.clicks().subscribe {
            val bundle = bundleOf(RES_ID to RES_ID_PRIVACY_POLICY)
            findNavController().navigate(
                R.id.action_AgreementsFragment_to_agreementFragment,
                bundle
            )
        }.also { compositeDisposable.add(it) }
        userAgreement.clicks().subscribe {
            val bundle = bundleOf(RES_ID to RES_ID_TERMS_OF_USE)
            findNavController().navigate(
                R.id.action_AgreementsFragment_to_agreementFragment,
                bundle
            )
        }.also { compositeDisposable.add(it) }
        copyrightAgreement.clicks().subscribe {
            val bundle = bundleOf(RES_ID to RES_ID_RIGHTHOLDERS)
            findNavController().navigate(
                R.id.action_AgreementsFragment_to_agreementFragment,
                bundle
            )
        }.also { compositeDisposable.add(it) }
    }

    private fun initObservers() {
        compositeDisposable.add(viewModel.isLoading
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ showLoading(it) },
                { Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show() }))

        compositeDisposable.add(viewModel.isNext
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe ({
                if (it) {
                    toSplash()
                }
            }, { Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show() }))

        compositeDisposable.add(viewModel.errorState
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ errorHandler.handle(it) },
                { Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show() }))
    }
}
