package com.intergroupapplication.presentation.feature.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.intergroupapplication.R
import com.intergroupapplication.data.session.UserSession
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class SplashFragment : Fragment() {

    @Inject
    lateinit var userSession: UserSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when {
            !userSession.isAcceptTerms() -> findNavController()
                .navigate(R.id.action_splashFragment_to_AgreementsFragment)
            !userSession.isLoggedIn() -> findNavController()
                .navigate(R.id.action_splashFragment_to_registrationFragment)
            else -> findNavController().navigate(R.id.action_splashFragment_to_newsFragment)
        }
    }


}
