package com.intergroupapplication.presentation.feature.mainActivity.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.fragment.app.FragmentActivity
import com.intergroupapplication.R
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.domain.exception.*
import com.intergroupapplication.initializators.ErrorHandlerInitializer
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.feature.agreements.view.AgreementsActivity
import com.intergroupapplication.presentation.feature.confirmationmail.view.ConfirmationMailScreen
import com.intergroupapplication.presentation.feature.createuserprofile.view.CreateUserProfileScreen
import com.intergroupapplication.presentation.feature.login.view.LoginScreen
import com.intergroupapplication.presentation.feature.navigation.view.NavigationActivity
import com.intergroupapplication.presentation.feature.registration.view.RegistrationScreen
import com.workable.errorhandler.Action
import com.workable.errorhandler.ErrorHandler
import dagger.android.AndroidInjection
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import java.net.UnknownHostException
import javax.inject.Inject

class MainActivity : FragmentActivity() {

    @Inject
    lateinit var userSession: UserSession

    lateinit var compositeDisposable: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        setContentView(R.layout.activity_main)
        compositeDisposable = CompositeDisposable()
    }

}