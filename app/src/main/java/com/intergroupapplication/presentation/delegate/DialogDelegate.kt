package com.intergroupapplication.presentation.delegate

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.androidadvance.topsnackbar.R.id
import com.androidadvance.topsnackbar.TSnackbar
import com.intergroupapplication.R
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.provider.DialogProvider

/**
 * Created by abakarmagomedov on 02/08/2018 at project InterGroupApplication.
 */
class DialogDelegate(
    private val dialogManager: DialogManager,
    private val dialogProvider: DialogProvider,
    private val context: Context
) {

    private companion object {
        const val INTERGROUP_DIALOG = "inter_group_dialog"
    }

    var coordinator: ViewGroup? = null

    private val progressDialog = dialogProvider.newProgressDialog()

    fun showDialog(dialogLayout: Int, actionsMap: Map<Int, () -> Unit>) {
        dialogProvider.newDialog(dialogLayout, actionsMap).show(
            dialogManager.getManager(),
            INTERGROUP_DIALOG
        )
    }

    fun showPreviewDialog(
        isPhoto: Boolean,
        url: String,
        isChoose: Boolean,
        previewVideo: String = ""
    ) {
        dialogProvider.newPreviewDialog(isPhoto, url, isChoose, previewVideo)
            .show(dialogManager.getManager(), INTERGROUP_DIALOG)
    }

    fun showProgressDialog() {
        progressDialog.show(dialogManager.getManager(), INTERGROUP_DIALOG)
    }

    fun dismissProgressDialog() {
        progressDialog.dismiss()
    }

    fun showErrorSnackBar(message: String) {
        coordinator?.let {
            TSnackbar.make(it, message, TSnackbar.LENGTH_SHORT)
                .apply {
                    view.setBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.errorSnackBarColor
                        )
                    )
                    view.findViewById<TextView>(id.snackbar_text)
                        .setTextColor(Color.WHITE)
                }
                .show()
        }
    }
}
