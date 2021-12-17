package com.intergroupapplication.presentation.base

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.intergroupapplication.R

/**
 * Created by abakarmagomedov on 02/08/2018 at project InterGroupApplication.
 */
class BaseDialogResolver : DialogFragment() {

    companion object {
        const val LAYOUT_NOT_FOUND = -500
    }

    var actionsMap: Map<Int, () -> Unit> = emptyMap()
    var dialogLayout: Int = LAYOUT_NOT_FOUND

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val sameDialogs = mutableListOf<BaseDialogResolver>()
        addDialogToList(parentFragmentManager, sameDialogs)
        dropDialogIfMoreThanOne(sameDialogs)
        val dialogView = prepareView()
        val builder = AlertDialog.Builder(requireActivity())
        setActionsToViews(actionsMap, dialogView)
        builder.setView(dialogView)
        return builder.create().apply {
            window?.attributes?.windowAnimations = R.style.DialogTheme
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }
    }

    private fun setActionsToViews(actionsMap: Map<Int, () -> Unit>, dialogView: View) {
        for (entry in actionsMap) {
            dialogView.findViewById<View>(entry.key).setOnClickListener {
                entry.value.invoke()
                dismiss()
            }
        }
    }

    private fun prepareView(): View {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(dialogLayout, null)
        view.findViewById<Button>(R.id.cancelButton)?.apply {
            setOnClickListener { dismiss() }
        }
        return view
    }

    private fun addDialogToList(
        fragmentManager: FragmentManager?,
        sameDialogs: MutableList<BaseDialogResolver>
    ) {
        fragmentManager?.fragments?.let {
            it.forEach { fragment ->
                if (fragment is BaseDialogResolver) {
                    sameDialogs.add(fragment)
                }
            }
        }
    }

    private fun dropDialogIfMoreThanOne(sameDialogs: MutableList<BaseDialogResolver>) {
        if (sameDialogs.size > 1) {
            sameDialogs.dropLast(1).forEach { it.dismiss() }
        }
    }
}
