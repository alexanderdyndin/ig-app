package com.intergroupapplication.presentation.exstension

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.intergroupapplication.R


/**
 * Created by abakarmagomedov on 01/08/2018 at project InterGroupApplication.
 */

private const val SWIPE_OFFSET = 200

fun Context.dpToPx(dp: Int): Int {
    val displayMetrics = this.resources.displayMetrics
    return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
}

fun Context.pxToDp(px: Int): Int {
    val displayMetrics = this.resources.displayMetrics
    return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
}

fun AppCompatActivity.swipeLayoutUnderToolbar(swipeLayout: SwipeRefreshLayout) {
    val tv = TypedValue()
    theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)
    val actionBarHeight = resources.getDimensionPixelSize(tv.resourceId) + 20
    val end = actionBarHeight + SWIPE_OFFSET
    swipeLayout.setProgressViewOffset(false, actionBarHeight, end)
    //swipeLayout.setProgressViewEndTarget(false, end)
}

fun Fragment.swipeLayoutUnderToolbar(swipeLayout: SwipeRefreshLayout) {
    val tv = TypedValue()
    activity?.theme?.resolveAttribute(android.R.attr.actionBarSize, tv, true)
    val actionBarHeight = resources.getDimensionPixelSize(tv.resourceId) + 20
    val end = actionBarHeight + SWIPE_OFFSET
    swipeLayout.setProgressViewOffset(false, actionBarHeight, end)
}

fun Context.getGroupFollowersCount(followersCount: Int) =
        this.getString(if (followersCount == 1) R.string.member else R.string.members,
                followersCount.toString())

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

