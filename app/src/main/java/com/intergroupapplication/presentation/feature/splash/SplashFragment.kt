package com.intergroupapplication.presentation.feature.splash

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.navigation.fragment.findNavController
import com.intergroupapplication.R
import com.intergroupapplication.presentation.base.BaseFragment

class SplashFragment : BaseFragment() {

    @LayoutRes
    override fun layoutRes() = R.layout.fragment_splash

    override fun getSnackBarCoordinator(): CoordinatorLayout? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when {
            !userSession.isAcceptTerms() -> findNavController().navigate(R.id.action_splashActivity_to_AgreementsFragment2)
            !userSession.isLoggedIn() -> findNavController().navigate(R.id.action_splashActivity_to_registrationActivity)
            else -> findNavController().navigate(R.id.action_splashActivity_to_newsFragment2)
        }
    }


}
