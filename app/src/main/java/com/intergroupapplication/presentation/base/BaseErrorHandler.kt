package com.intergroupapplication.presentation.base

import com.intergroupapplication.R
import com.intergroupapplication.presentation.manager.ResourceManager
import timber.log.Timber
import java.io.IOException
import java.lang.ref.WeakReference

/**
 * Created by abakarmagomedov on 31/07/2018 at project InterGroupApplication.
 */
class BaseErrorHandler constructor(private val resourceManager: ResourceManager) : ErrorHandler {

    private lateinit var view: WeakReference<CanShowError>

    override fun proceed(error: Throwable) {
        Timber.e(error)
        val view = view.get()
                ?: throw IllegalStateException("View must be attached to ErrorHandler")

        val message = when (error::class) {
            IOException::class -> resourceManager.getString(R.string.lost_connection)
            else -> resourceManager.getString(R.string.unknown_error)
        }
        view.showError(message)
    }

    override fun attachView(view: CanShowError) {
        this.view = WeakReference(view)
    }

    override fun detachView() {
        view.clear()
    }

}
