package com.intergroupapplication.presentation.delegate

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import android.view.ViewGroup
import android.widget.TextView
import com.androidadvance.topsnackbar.R.*
import com.androidadvance.topsnackbar.TSnackbar
import com.intergroupapplication.R
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.manager.DialogProvider
import com.intergroupapplication.presentation.manager.ToastManager

/**
 * Created by abakarmagomedov on 02/08/2018 at project InterGroupApplication.
 */
class DialogDelegate(private val dialogManager: DialogManager,
                     private val dialogProvider: DialogProvider,
                     private val toastManager: ToastManager,
                     private val context: Context) {

    companion object {
        const val INTERGROUP_DIALOG = "inter_group_dialog"
    }

    var coordinator: ViewGroup? = null

    fun setCoord(coordinator: ViewGroup) {
        this.coordinator = coordinator
    }

    fun showDialog(dialogLayout: Int, actionsMap: Map<Int, () -> Unit>) {
        dialogProvider.newDialog(dialogLayout, actionsMap).show(dialogManager.getManager(), INTERGROUP_DIALOG)
    }

    fun showErrorSnackBar(message: String) {
        coordinator?.let {
            TSnackbar.make(it, message, TSnackbar.LENGTH_SHORT)
                    .apply {
                        view.setBackgroundColor(ContextCompat.getColor(context, R.color.errorSnackBarColor))
                        view.findViewById<TextView>(id.snackbar_text)
                                .setTextColor(Color.WHITE)
                    }
                    .show()
        }
    }

    fun showErrorToast(message: String) {
        toastManager.showErrorToast(message)
    }
}
