package com.intergroupapplication.presentation.feature.splash

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.navigation.fragment.findNavController
import com.intergroupapplication.R
import com.intergroupapplication.presentation.base.BaseFragment

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
//            !userSession.isAcceptTerms() -> //router.newRootScreen(AgreementsScreen())
//            !userSession.isLoggedIn() -> //router.newRootScreen(RegistrationScreen())
//            else -> //router.newRootScreen(NavigationScreen())
            !userSession.isAcceptTerms() -> findNavController().navigate(R.id.action_splashActivity_to_agreementsActivity2)
            !userSession.isLoggedIn() -> findNavController().navigate(R.id.action_splashActivity_to_registrationActivity)
            else -> findNavController().navigate(R.id.action_splashActivity_to_newsFragment2)
        }
    }


}
