package com.intergroupapplication.presentation.feature.commentsdetails.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.intergroupapplication.R

class BaseFullBottomSheetFragment : BottomSheetDialogFragment() {
    /**
     * Смещение сверху вниз
     */
    var topOffset = 0
    var behavior: BottomSheetBehavior<FrameLayout>? = null
        private set

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return if (context != null) {
            BottomSheetDialog(requireContext(), R.style.TransparentBottomSheetStyle)
        } else super.onCreateDialog(savedInstanceState)
    }



    override fun onStart() {
        super.onStart()
        // Устанавливаем программную клавиатуру, чтобы она не появлялась автоматически
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        val dialog: BottomSheetDialog = dialog as BottomSheetDialog
        val bottomSheet: FrameLayout? = dialog.delegate.findViewById(
               R.id.design_bottom_sheet)
        if (bottomSheet != null) {
            //val layoutParams = bottomSheet.layoutParams as CoordinatorLayout.LayoutParams
            //layoutParams.height = height
            behavior = BottomSheetBehavior.from(bottomSheet)
            // Изначально расширен
            behavior?.isFitToContents = false
            behavior?.peekHeight = 300
        }
    }// Используем Point, чтобы вычесть высоту строки состояния

    /**
     * Получить высоту экрана
     *
     * @return height
     */
   /* private val height: Int
        private get() {
            var height = 1920
            if (getContext() != null) {
                val wm = getContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val point = Point()
                if (wm != null) {
                    // Используем Point, чтобы вычесть высоту строки состояния
                    wm.defaultDisplay.getSize(point)
                    height = point.y - topOffset
                }
            }
            return height
        }*/
}