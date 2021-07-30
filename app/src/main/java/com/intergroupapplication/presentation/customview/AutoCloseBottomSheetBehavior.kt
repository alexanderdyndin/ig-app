package com.intergroupapplication.presentation.customview

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior


class AutoCloseBottomSheetBehavior<V : View>(context: Context, attrs: AttributeSet?) : BottomSheetBehavior<V>(context, attrs) {

    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN &&
                state == STATE_HALF_EXPANDED) {
            val outRect = Rect()
            child.getGlobalVisibleRect(outRect)
            if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                state = STATE_COLLAPSED
            }
        }

        return super.onInterceptTouchEvent(parent, child, event)
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: V, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        return if (state == STATE_HALF_EXPANDED){
            false
        }
        else
            super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type)
    }
}