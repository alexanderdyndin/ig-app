package com.intergroupapplication.presentation.base

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import moxy.MvpAppCompatActivity
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
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
//import dagger.android.support.HasSupportFragmentInjector
import dagger.android.HasAndroidInjector
import io.reactivex.disposables.CompositeDisposable
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import java.net.UnknownHostException
import javax.inject.Inject

abstract class BaseActivity : MvpAppCompatActivity(), /*HasSupportFragmentInjector,*/ HasAndroidInjector {

    companion object {
        const val PASSWORD_REQUIRED_LENGTH = 8
    }

//    @Inject
//    lateinit var supportFragmentInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var supportFragmentInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var navigatorHolder: NavigatorHolder

    @Inject
    lateinit var errorHandlerInitializer: ErrorHandlerInitializer

    @Inject
    lateinit var errorHandler: ErrorHandler

    @Inject
    protected lateinit var dialogDelegate: DialogDelegate

    @Inject
    protected lateinit var userSession: UserSession

    @Inject
    protected lateinit var router: Router

    protected lateinit var compositeDisposable: CompositeDisposable

    abstract var navigator: SupportAppNavigator


    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(layoutRes())
        compositeDisposable = CompositeDisposable()
        initErrorHandler()
        viewCreated()
    }

    override fun onResume() {
        super.onResume()
        navigatorHolder.setNavigator(navigator)
        dialogDelegate.coordinator = getSnackBarCoordinator()
    }

    override fun onPause() {
        navigatorHolder.removeNavigator()
        dialogDelegate.coordinator = null
        super.onPause()
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    //override fun supportFragmentInjector(): AndroidInjector<Fragment> = supportFragmentInjector

    override fun androidInjector() = supportFragmentInjector

    @LayoutRes
    protected abstract fun layoutRes(): Int

    protected abstract fun getSnackBarCoordinator(): ViewGroup?

    protected abstract fun viewCreated()

    protected fun showErrorMessage(message: String) {
        dialogDelegate.showErrorSnackBar(message)
    }

    protected fun initErrorHandler() {
        errorHandler.clear()

        val errorMap = mapOf(
                BadRequestException::class.java to
                        Action { throwable, _ -> dialogDelegate.showErrorSnackBar((throwable as BadRequestException).message) },
                UserBlockedException::class.java to getActionForBlockedUser(),
                ServerException::class.java to
                        Action { _, _ -> dialogDelegate.showErrorSnackBar(getString(R.string.server_error)) },
                NotFoundException::class.java to
                        Action { throwable, _ -> dialogDelegate.showErrorSnackBar((throwable as NotFoundException).message.orEmpty()) },
                UnknownHostException::class.java to createSnackBarAction(R.string.no_network_connection),
                CanNotUploadPhoto::class.java to createToast(R.string.can_not_change_avatar),
                UserNotProfileException::class.java to openCreateProfile(),
                GroupBlockedException::class.java to getActionForBlockedGroup(),
                UserNotVerifiedException::class.java to openConfirmationEmail(),
                ImeiException::class.java to getActionForBlockedImei(),
                InvalidRefreshException::class.java to openAutorize(),
                //todo исправить пагинацию
                PageNotFoundException::class.java to Action { _, _ -> })

        errorHandlerInitializer.initializeErrorHandler(errorMap,
                createSnackBarAction(R.string.unknown_error))
    }

    private fun getActionForBlockedImei() = Action { throwable, _ ->
        startActivity(Intent(this, AgreementsActivity::class.java))
        dialogDelegate.showErrorSnackBar((throwable as ImeiException).message.orEmpty())
        userSession.clearAllData()
    }

    private fun getActionForBlockedUser() =
            if (userSession.isLoggedIn()) {
                actionForBlockedUser
            } else {
                createSnackBarAction(R.string.user_blocked)
            }


    private fun getActionForBlockedGroup() = actionForBlockedGroup

    private val actionForBlockedGroup = Action { _, _ ->
        startActivity(Intent(this, NavigationActivity::class.java).also
        { it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) })
    }

    private val actionForBlockedUser = Action { _, _ ->
        userSession.clearAllData()
        startActivity(Intent(this, AgreementsActivity::class.java).also
        { it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) })
    }

    private fun createSnackBarAction(message: Int) =
            Action { _, _ -> dialogDelegate.showErrorSnackBar(getString(message)) }

    private fun createToast(message: Int) =
            Action { _, _ -> Toast.makeText(this, getString(message), Toast.LENGTH_SHORT).show() }

    private fun openCreateProfile() = Action { _, _ ->
        router.newRootScreen(CreateUserProfileScreen())
    }

    private fun openConfirmationEmail() = Action { _, _ ->
        val email = userSession.email?.email.orEmpty()
        router.newRootChain(RegistrationScreen(), ConfirmationMailScreen(email))
    }

    private fun openAutorize() = Action { _, _ ->
        userSession.logout()
        router.newRootChain(LoginScreen())
    }
}

