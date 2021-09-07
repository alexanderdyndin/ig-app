package com.intergroupapplication.domain

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import java.lang.ref.WeakReference

private const val KEYBOARD_MIN_HEIGHT_RATIO = 0.15

object KeyboardVisibilityEvent {

    @Suppress("unused")
    @JvmStatic
    fun setEventListener(
        activity: Activity,
        lifecycleOwner: LifecycleOwner,
        listener: KeyboardVisibilityEventListener
    ) {

        val unregister = registerEventListener(activity, listener)
        lifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                lifecycleOwner.lifecycle.removeObserver(this)
                unregister.unregister()
            }
        })
    }

    private fun registerEventListener(
        activity: Activity?,
        listener: KeyboardVisibilityEventListener?
    ): Unregister {

        if (activity == null) {
            throw NullPointerException("Parameter:activity must not be null")
        }

        val softInputAdjust =
            activity.window.attributes.softInputMode and WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST

        val isNotAdjustNothing =
            softInputAdjust and WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING != WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
        require(isNotAdjustNothing) { "Parameter:activity window SoftInputMethod is SOFT_INPUT_ADJUST_NOTHING. In this case window will not be resized" }

        if (listener == null) {
            throw NullPointerException("Parameter:listener must not be null")
        }

        val activityRoot = getActivityRoot(activity)

        val layoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {

            private var wasOpened = false

            override fun onGlobalLayout() {

                val isOpen = isKeyboardVisible(activity)

                if (isOpen == wasOpened) {
                    return
                }

                wasOpened = isOpen

                listener.onVisibilityChanged(isOpen)
            }
        }
        activityRoot.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)

        return SimpleUnregister(activity, layoutListener)
    }

    private fun isKeyboardVisible(activity: Activity): Boolean {
        val r = Rect()

        val activityRoot = getActivityRoot(activity)

        activityRoot.getWindowVisibleDisplayFrame(r)

        val location = IntArray(2)
        getContentRoot(activity).getLocationOnScreen(location)

        val screenHeight = activityRoot.rootView.height
        val heightDiff = screenHeight - r.height() - location[1]

        return heightDiff > screenHeight * KEYBOARD_MIN_HEIGHT_RATIO
    }

    internal fun getActivityRoot(activity: Activity): View {
        return getContentRoot(activity).rootView
    }

    private fun getContentRoot(activity: Activity): ViewGroup {
        return activity.findViewById(android.R.id.content)
    }
}

fun interface KeyboardVisibilityEventListener {
    fun onVisibilityChanged(isOpen: Boolean)
}

private interface Unregister {

    fun unregister()
}

private class SimpleUnregister constructor(
    activity: Activity,
    globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener
) : Unregister {

    private val activityWeakReference: WeakReference<Activity> = WeakReference(activity)

    private val onGlobalLayoutListenerWeakReference: WeakReference<ViewTreeObserver.OnGlobalLayoutListener> =
        WeakReference(globalLayoutListener)

    override fun unregister() {
        val activity = activityWeakReference.get()
        val globalLayoutListener = onGlobalLayoutListenerWeakReference.get()

        if (null != activity && null != globalLayoutListener) {
            val activityRoot = KeyboardVisibilityEvent.getActivityRoot(activity)
            activityRoot.viewTreeObserver
                .removeOnGlobalLayoutListener(globalLayoutListener)
        }

        activityWeakReference.clear()
        onGlobalLayoutListenerWeakReference.clear()
    }
}
