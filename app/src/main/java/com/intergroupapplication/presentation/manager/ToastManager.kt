package com.intergroupapplication.presentation.manager

import android.content.Context
import android.widget.Toast
import es.dmoral.toasty.Toasty
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 03/08/2018 at project InterGroupApplication.
 */
class ToastManager @Inject constructor(private val context: Context) {

    fun showErrorToast(message: String) {
        Toasty.error(context, message, Toast.LENGTH_SHORT).show()
    }
}
