package com.intergroupapplication.presentation.feature.agreements.presenter

import moxy.InjectViewState
import com.intergroupapplication.R
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.domain.entity.TermsEntity
import com.intergroupapplication.domain.gateway.ImeiGateway
import com.intergroupapplication.domain.gateway.PermissionAutorizeGetaway
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.exstension.handleLoading
import com.intergroupapplication.presentation.feature.ActionApplicationDetailsScreen
import com.intergroupapplication.presentation.feature.agreements.view.AgreementsView
import com.intergroupapplication.presentation.feature.splash.SplashScreen
import com.intergroupapplication.presentation.feature.web.WebScreen
import com.workable.errorhandler.ErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@InjectViewState
class AgreementsPresenter @Inject constructor(private val router: Router,
                                              private val errorHandler: ErrorHandler,
                                              private val imeiGateway: ImeiGateway,
                                              private val userSession: UserSession,
                                              private val permissionGetaway: PermissionAutorizeGetaway)
    : BasePresenter<AgreementsView>() {

    companion object {
        private const val URL_PRIVACY_POLICY = "https://igsn.net/agreement/1.html"
        private const val URL_TERMS_OF_USE = "https://igsn.net/agreement/2.html"
        private const val URL_RIGHTHOLDERS = "https://igsn.net/agreement/3.html"
        private const val URL_APPODEAL = "https://www.appodeal.com/home/privacy-policy/"

        private const val RES_ID_PRIVACY_POLICY = R.string.privacy_police
        private const val RES_ID_TERMS_OF_USE = R.string.terms_of_use
        private const val RES_ID_RIGHTHOLDERS = R.string.rightholders
        private const val RES_ID_APPODEAL = R.string.appodealpolicy
    }


    fun next() {
        compositeDisposable.add(imeiGateway.extractDeviceInfo()
                .subscribeOn(Schedulers.io())
                .andThen(permissionGetaway.isBlocked())
                .observeOn(AndroidSchedulers.mainThread())
                .handleLoading(viewState)
                .subscribe({
                    userSession.acceptTerms = TermsEntity(true)
                    viewState.toSplash()
                }, { errorHandler.handle(it) }))
    }

    fun goToSettingsScreen() {
        router.navigateTo(ActionApplicationDetailsScreen())
    }

}