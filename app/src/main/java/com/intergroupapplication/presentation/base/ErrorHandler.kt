package com.intergroupapplication.presentation.base

/**
 * Created by abakarmagomedov on 31/07/2018 at project InterGroupApplication.
 */
interface ErrorHandler {
    fun proceed(error: Throwable)
    fun attachView(view: CanShowError)
    fun detachView()
}
