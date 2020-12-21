package com.intergroupapplication.presentation.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.LayoutRes
import com.intergroupapplication.R
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.domain.exception.*
import com.intergroupapplication.initializators.ErrorHandlerInitializer
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.workable.errorhandler.Action
import com.workable.errorhandler.ErrorHandler
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import moxy.MvpAppCompatFragment

import java.net.UnknownHostException
import javax.inject.Inject

abstract class BaseFragment : MvpAppCompatFragment() {

    @LayoutRes
    protected abstract fun layoutRes(): Int

    /**
     *     BaseActivity compatibility
     */

    protected lateinit var compositeDisposable: CompositeDisposable

    protected abstract fun getSnackBarCoordinator(): ViewGroup?

    @Inject
    protected lateinit var userSession: UserSession

    @Inject
    lateinit var errorHandlerInitializer: ErrorHandlerInitializer

    @Inject
    lateinit var errorHandler: ErrorHandler

    @Inject
    protected lateinit var dialogDelegate: DialogDelegate


    private fun initErrorHandler() {
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
                PageNotFoundException::class.java to Action { _, _ -> })

        errorHandlerInitializer.initializeErrorHandler(errorMap,
                createSnackBarAction(R.string.unknown_error))
    }

    private fun getActionForBlockedImei() = Action { throwable, _ ->
        //        startActivity(Intent(this, AgreementsActivity::class.java))
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
//        startActivity(Intent(this, NavigationActivity::class.java).also
//        { it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) })
    }

    private val actionForBlockedUser = Action { _, _ ->
        userSession.clearAllData()
//        startActivity(Intent(this, AgreementsActivity::class.java).also
//        { it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) })
    }

    private fun createSnackBarAction(message: Int) =
            Action { _, _ -> dialogDelegate.showErrorSnackBar(getString(message)) }

    private fun createToast(message: Int) =
            Action { _, _ -> Toast.makeText(requireContext(), getString(message), Toast.LENGTH_SHORT).show() }

    fun showToast(message: String) =
            Action { _, _ -> Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show() }

    protected fun showErrorMessage(message: String) {
        dialogDelegate.showErrorSnackBar(message)
    }

    private fun openCreateProfile() = Action { _, _ ->
    }

    private fun openConfirmationEmail() = Action { _, _ ->
        val email = userSession.email?.email.orEmpty()
    }

    private fun openAutorize() = Action { _, _ ->
        userSession.logout()
    }


    /**
     *
     */

    open fun viewCreated() {}

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        compositeDisposable = CompositeDisposable()
        initErrorHandler()
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(layoutRes(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewCreated()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        dialogDelegate.coordinator = getSnackBarCoordinator()
    }

    override fun onPause() {
        dialogDelegate.coordinator = null
        super.onPause()
    }
}
