package com.intergroupapplication.presentation.listeners

import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText

/**
 * Created by abakarmagomedov on 22/08/2018 at project InterGroupApplication.
 */
class RightDrawableListener : View.OnTouchListener {

    companion object {
        const val CLICK_OFFSET = 40
    }

    var clickListener: () -> Unit = {}

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        val drawableRight = 2
        if (event.action == MotionEvent.ACTION_UP && view is AppCompatEditText
            && view.compoundDrawables[drawableRight] != null
        ) {

            if (event.rawX >= (view.right - view.compoundDrawables[drawableRight].bounds.width() - CLICK_OFFSET)) {
                view.text?.let {
                    clickListener.invoke()
                    it.clear()
                }
                return true
            } else {
                view.performClick()
            }
        } else {
            view.performClick()
        }
        return false
    }
}
