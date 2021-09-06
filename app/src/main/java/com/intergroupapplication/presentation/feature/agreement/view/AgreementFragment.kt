package com.intergroupapplication.presentation.feature.agreement.view

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModelProvider
import by.kirich1409.viewbindingdelegate.viewBinding
import com.intergroupapplication.R
import com.intergroupapplication.databinding.FragmentAgreementBinding
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.exstension.gone
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.agreement.viewmodel.AgreementViewModel
import com.intergroupapplication.presentation.feature.agreements.view.AgreementsFragment.Companion.RES_ID_PRIVACY_POLICY
import com.intergroupapplication.presentation.feature.agreements.view.AgreementsFragment.Companion.RES_ID_RIGHTHOLDERS
import com.intergroupapplication.presentation.feature.agreements.view.AgreementsFragment.Companion.RES_ID_TERMS_OF_USE
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class AgreementFragment: BaseFragment() {

    companion object {
        const val RES_ID = "resId"
    }

    private val viewBinding by viewBinding(FragmentAgreementBinding::bind)

    override fun layoutRes() = R.layout.fragment_agreement

    override fun getSnackBarCoordinator() = viewBinding.container

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: AgreementViewModel

    private var resId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, modelFactory)[AgreementViewModel::class.java]
        resId = arguments?.getInt(RES_ID)
        getTermsById(resId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        setListeners()
    }

    private fun initObservers() {
        compositeDisposable.add(viewModel.isLoading
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it) {
                    viewBinding.loadingError.errorLayout.gone()
                    viewBinding.loadingError.loadingLayout.show()
                } else {
                    viewBinding.loadingError.loadingLayout.gone()
                }
            })

        compositeDisposable.add(viewModel.value
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                viewBinding.text.text = HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_COMPACT)
            })

        compositeDisposable.add(viewModel.errorState
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                viewBinding.loadingError.errorLayout.show()
            })
    }

    private fun setListeners() {
        viewBinding.loadingError.buttonRetry.setOnClickListener {
            getTermsById(resId)
        }
    }

    private fun getTermsById(resId: Int?) {
        when(resId) {
            RES_ID_PRIVACY_POLICY -> viewModel.getPrivacy()
            RES_ID_RIGHTHOLDERS -> viewModel.getRightHolders()
            RES_ID_TERMS_OF_USE -> viewModel.getTerms()
        }
    }

}