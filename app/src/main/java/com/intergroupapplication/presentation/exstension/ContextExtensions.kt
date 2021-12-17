package com.intergroupapplication.presentation.exstension

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.DisplayMetrics
import android.view.View
import kotlin.math.roundToInt


/**
 * Created by abakarmagomedov on 01/08/2018 at project InterGroupApplication.
 */

fun Context.dpToPx(dp: Int): Int {
    val displayMetrics = this.resources.displayMetrics
    return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
}

fun View.getActivity(): Activity? {
    var context: Context? = context
    while (context is ContextWrapper) {
        if (context is Activity) {
            return context
        }
        context = context.baseContext
    }
    return null
}
