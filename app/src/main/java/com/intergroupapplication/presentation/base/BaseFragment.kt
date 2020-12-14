package com.intergroupapplication.presentation.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.intergroupapplication.data.session.UserSession
import dagger.android.support.AndroidSupportInjection
import moxy.MvpAppCompatFragment
import ru.terrakok.cicerone.Router
import javax.inject.Inject

abstract class BaseFragment : MvpAppCompatFragment() {

    @LayoutRes
    protected abstract fun layoutRes(): Int

    /**
     *     BaseActivity compatibility
     */

    protected open fun getSnackBarCoordinator(): ViewGroup? { return null}

    @Inject
    protected lateinit var router: Router

    @Inject
    protected lateinit var userSession: UserSession

    /**
     *
     */

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(layoutRes(), container, false)
}
