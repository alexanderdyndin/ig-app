package com.intergroupapplication.presentation.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.navigation.fragment.findNavController
import com.intergroupapplication.R
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.workable.errorhandler.ErrorHandler
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import moxy.MvpAppCompatFragment
import javax.inject.Inject

abstract class BaseFragment : MvpAppCompatFragment() {

    @LayoutRes
    protected abstract fun layoutRes(): Int

    companion object {
        const val GROUP_ID = "group_id"
    }

    protected lateinit var compositeDisposable: CompositeDisposable

    protected abstract fun getSnackBarCoordinator(): ViewGroup?

    @Inject
    protected lateinit var userSession: UserSession

    @Inject
    open lateinit var errorHandler: ErrorHandler

    @Inject
    protected lateinit var dialogDelegate: DialogDelegate

    protected fun showErrorMessage(message: String) {
        dialogDelegate.showErrorSnackBar(message)
    }

    open fun viewCreated() {}

    /**
     *
     */

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        compositeDisposable = CompositeDisposable()
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

    override fun onDestroyView() {
        compositeDisposable.clear()
        super.onDestroyView()
    }

    fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    fun showToast(msg: Int) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    fun logOut() {
        userSession.logout()
        findNavController().navigate(R.id.action_global_loginFragment)
    }
}
