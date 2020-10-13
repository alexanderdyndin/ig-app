package com.intergroupapplication.initializators

import com.workable.errorhandler.Action
import com.workable.errorhandler.ErrorHandler
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 21/08/2018 at project InterGroupApplication.
 */
class ErrorHandlerInitializer @Inject constructor(private val errorHandler: ErrorHandler) {

    fun initializeErrorHandler(errorMap: Map<Class<out Exception>, Action>, otherWiseAction: Action) {
        for (mutableEntry in errorMap) {
            errorHandler.on(mutableEntry.key, mutableEntry.value)
        }
        errorHandler.otherwise(otherWiseAction)
    }

}
