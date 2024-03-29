package com.intergroupapplication.presentation.customview

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout

class AutoCloseBottomSheetBehavior<V : View>(context: Context, attrs: AttributeSet?) :
    NestedScrollBottomSheetBehavior<V>(context, attrs) {

    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent):
            Boolean {
        if (event.action == MotionEvent.ACTION_DOWN &&
            state == STATE_HALF_EXPANDED
        ) {
            val outRect = Rect()
            child.getGlobalVisibleRect(outRect)
            if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                state = STATE_COLLAPSED
            }
        }

        return super.onInterceptTouchEvent(parent, child, event)
    }
}
