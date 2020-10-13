package com.intergroupapplication.presentation.delegate

import androidx.fragment.app.DialogFragment
import com.intergroupapplication.presentation.manager.DialogManager

/**
 * Created by abakarmagomedov on 03/08/2018 at project InterGroupApplication.
 */
class LoadingDelegate(private val dialogManager: DialogManager,
                      private val dialog: DialogFragment) {

    companion object {
        const val DIALOG_TAG = "intergroup_dialog"
    }

    fun showFullScreenLoading(show: Boolean) {
        if (show) {
            dialog.show(dialogManager.getManager(), DIALOG_TAG)
        } else {
            dialog.dismiss()
        }
    }
}
