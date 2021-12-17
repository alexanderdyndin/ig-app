package com.intergroupapplication.presentation.feature.splash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.intergroupapplication.R
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.presentation.feature.mainActivity.view.MainActivity
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class SplashFragment : Fragment(R.layout.fragment_splash) {

    @Inject
    lateinit var userSession: UserSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when {
            !userSession.isAcceptTerms() -> findNavController().navigate(R.id.action_global_AgreementsFragment)
            !userSession.isLoggedIn() -> findNavController().navigate(R.id.action_global_registrationFragment)
            else -> {
                val activity = requireActivity()
                if (activity is MainActivity) {
                    activity.createDrawer()
                } else throw IllegalStateException("Wrong Activity")
            }
        }
    }
}
