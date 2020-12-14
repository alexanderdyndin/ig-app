package com.intergroupapplication.presentation.feature.splash

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.annotation.LayoutRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.navigation.fragment.findNavController
import com.intergroupapplication.R
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.presentation.base.BaseActivity
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.feature.agreements.view.AgreementsScreen
import com.intergroupapplication.presentation.feature.createuserprofile.view.CreateUserProfileScreen
import com.intergroupapplication.presentation.feature.login.view.LoginScreen
import com.intergroupapplication.presentation.feature.navigation.view.NavigationScreen
import com.intergroupapplication.presentation.feature.registration.view.RegistrationActivity
import com.intergroupapplication.presentation.feature.registration.view.RegistrationScreen
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import javax.inject.Inject

class SplashActivity : BaseFragment() {

    companion object {
        fun getIntent(context: Context?) = Intent(context, SplashActivity::class.java)
    }

    @LayoutRes
    override fun layoutRes() = R.layout.activity_splash

    override fun getSnackBarCoordinator(): CoordinatorLayout? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when {
//            !userSession.isAcceptTerms() -> router.newRootScreen(AgreementsScreen())
//            !userSession.isLoggedIn() -> router.newRootScreen(RegistrationScreen())
//            else -> router.newRootScreen(NavigationScreen())
            !userSession.isAcceptTerms() -> findNavController().navigate(R.id.action_splashActivity_to_agreementsActivity)
            !userSession.isLoggedIn() -> findNavController().navigate(R.id.action_splashActivity_to_registrationActivity)
            else -> findNavController().navigate(R.id.action_splashActivity_to_newsFragment)
        }
    }


}
