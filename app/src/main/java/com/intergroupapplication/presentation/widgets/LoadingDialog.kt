package com.intergroupapplication.presentation.widgets

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.intergroupapplication.R
import com.intergroupapplication.presentation.exstension.dpToPx
import com.wang.avi.AVLoadingIndicatorView

/**
 * Created by abakarmagomedov on 06/08/2018 at project InterGroupApplication.
 */
class LoadingDialog : DialogFragment() {

    companion object {
        const val DIALOG_HEIGHT = 148
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_dialog_loading, null)
        val loader = view.findViewById<AVLoadingIndicatorView>(R.id.loader)
        loader.show()
        builder.setView(view)
        return builder.create()
    }

    override fun onResume() {
        super.onResume()
        val metrics = DisplayMetrics()
        val window = dialog?.window
        window!!.windowManager.defaultDisplay.getMetrics(metrics)
        val dialogHeightPx = requireContext().dpToPx(DIALOG_HEIGHT)
        window.setLayout(dialogHeightPx, dialogHeightPx)
        window.setGravity(Gravity.CENTER)
    }

}
