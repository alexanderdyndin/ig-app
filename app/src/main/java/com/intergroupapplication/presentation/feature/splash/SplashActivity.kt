package com.intergroupapplication.presentation.feature.splash

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.annotation.LayoutRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.intergroupapplication.R
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.presentation.base.BaseActivity
import com.intergroupapplication.presentation.feature.agreements.view.AgreementsScreen
import com.intergroupapplication.presentation.feature.createuserprofile.view.CreateUserProfileScreen
import com.intergroupapplication.presentation.feature.login.view.LoginScreen
import com.intergroupapplication.presentation.feature.navigation.view.NavigationScreen
import com.intergroupapplication.presentation.feature.registration.view.RegistrationActivity
import com.intergroupapplication.presentation.feature.registration.view.RegistrationScreen
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import javax.inject.Inject

class SplashActivity : BaseActivity() {

    companion object {
        fun getIntent(context: Context?) = Intent(context, SplashActivity::class.java)
    }

    @Inject
    override lateinit var navigator: SupportAppNavigator

    @LayoutRes
    override fun layoutRes() = R.layout.activity_splash

    override fun getSnackBarCoordinator(): CoordinatorLayout? = null

    override fun viewCreated() {
        when {
            !userSession.isAcceptTerms() -> router.newRootScreen(AgreementsScreen())
            !userSession.isLoggedIn() -> router.newRootScreen(RegistrationScreen())
            else -> router.newRootScreen(NavigationScreen())
        }
    }


}
