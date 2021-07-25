package com.intergroupapplication.presentation.base

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.intergroupapplication.App
import com.intergroupapplication.R
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.domain.exception.*
import com.intergroupapplication.initializators.ErrorHandlerInitializer
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.ExitActivity
import com.workable.errorhandler.Action
import com.workable.errorhandler.ErrorHandler
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import moxy.MvpAppCompatFragment
import timber.log.Timber

import java.net.UnknownHostException
import javax.inject.Inject

abstract class BaseFragment : MvpAppCompatFragment() {

    @LayoutRes
    protected abstract fun layoutRes(): Int

    companion object {
        const val GROUP_ID = "group_id"
    }

    /**
     *     BaseActivity compatibility
     */

    protected lateinit var compositeDisposable: CompositeDisposable

    protected abstract fun getSnackBarCoordinator(): ViewGroup?

    @Inject
    protected lateinit var userSession: UserSession

    lateinit var errorHandlerInitializer: ErrorHandlerInitializer

    @Inject
    lateinit var errorHandler: ErrorHandler

    @Inject
    protected lateinit var dialogDelegate: DialogDelegate


    fun initErrorHandler(errorHandler: ErrorHandler) {
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
                CanNotUploadVideo::class.java to createToast(R.string.can_not_upload_video),
                CanNotUploadAudio::class.java to createToast(R.string.can_not_upload_audio),
                UserNotProfileException::class.java to openCreateProfile(),
                GroupBlockedException::class.java to getActionForBlockedGroup(),
                UserNotVerifiedException::class.java to openConfirmationEmail(),
                ForbiddenException::class.java to createSnackBarAction(R.string.forbidden_error),
                ImeiException::class.java to getActionForBlockedImei(),
                InvalidRefreshException::class.java to openAutorize(),
                GroupAlreadyFollowingException::class.java to Action { _, _ ->},
                PageNotFoundException::class.java to
                        Action { throwable, _ ->
                dialogDelegate.showErrorSnackBar((throwable as PageNotFoundException).message.orEmpty())},
                UnknowServerException::class.java to Action { _, _ ->
                    createSnackBarAction(R.string.unknown_error)},
                ConnectionException::class.java to Action {_, _ -> dialogDelegate.
                    showErrorSnackBar(getString(R.string.no_network_connection))}
                )
        errorHandlerInitializer = ErrorHandlerInitializer(errorHandler)
        errorHandlerInitializer.initializeErrorHandler(errorMap,
                createSnackBarAction(R.string.unknown_error))
    }

    protected open fun getActionForBlockedImei() = Action { throwable, _ ->
        userSession.clearAllData()
        ExitActivity.exitApplication(requireContext())
    }

    private fun getActionForBlockedUser() =
            if (userSession.isLoggedIn()) {
                actionForBlockedUser()
            } else {
                createSnackBarAction(R.string.user_blocked)
            }


    protected open fun getActionForBlockedGroup() = actionForBlockedGroup

    private val actionForBlockedGroup = Action { _, _ ->
        dialogDelegate.showErrorSnackBar("Группа заблокирована")
        findNavController().popBackStack()
    }

    protected open fun actionForBlockedUser() = Action { _, _ ->
        userSession.logout()
        ExitActivity.exitApplication(requireContext())
    }

    protected fun createSnackBarAction(message: Int) =
            Action { _, _ -> dialogDelegate.showErrorSnackBar(getString(message)) }

    protected fun createToast(message: Int) =
            Action { _, _ -> Toast.makeText(requireContext(), getString(message), Toast.LENGTH_SHORT).show() }

    protected fun showToast(message: String) =
            Action { _, _ -> Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show() }

    protected fun showErrorMessage(message: String) {
        dialogDelegate.showErrorSnackBar(message)
    }

    protected open fun openCreateProfile() = Action { _, _ ->
        Timber.e("403 catched")
    }

    protected open fun openConfirmationEmail() = Action { _, _ ->
        Timber.e("403 catched")
    }

    private fun openAutorize() = Action { _, _ ->
        userSession.logout()
    }

    open fun viewCreated() { }

    /**
     *
     */

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        compositeDisposable = CompositeDisposable()
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(layoutRes(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initErrorHandler(errorHandler)
        viewCreated()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        dialogDelegate.coordinator = getSnackBarCoordinator()
    }

    override fun onPause() {
        dialogDelegate.coordinator = null
        //compositeDisposable.clear()
        super.onPause()
    }

}
