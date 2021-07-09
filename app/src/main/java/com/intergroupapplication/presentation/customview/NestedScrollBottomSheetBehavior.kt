package com.intergroupapplication.presentation.customview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior


open class NestedScrollBottomSheetBehavior<V : View>(context: Context, attrs: AttributeSet?) :
    BottomSheetBehavior<V>(context, attrs) {

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: V,
                                     directTargetChild: View, target: View, axes: Int, type: Int):
            Boolean {
        return if (state == STATE_HALF_EXPANDED){
            false
        }
        else
            super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes,
                type)
    }
}