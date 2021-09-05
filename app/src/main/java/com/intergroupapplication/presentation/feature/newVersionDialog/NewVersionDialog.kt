package com.intergroupapplication.presentation.feature.newVersionDialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment


class NewVersionDialog : DialogFragment() {

    private val NEW_VERSION_URL =
        "https://context.reverso.net/translation/russian-english/%D0%92%D1%8B%D1%88%D0%BB%D0%B0+%D0%BD%D0%BE%D0%B2%D0%B0%D1%8F+%D0%B2%D0%B5%D1%80%D1%81%D0%B8%D1%8F"

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Вышла новая версия приложения!")
                .setMessage("Вам необходимо обновиться для продолжения работы")
                .setPositiveButton("ОК") { _, _ ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(NEW_VERSION_URL))
                        startActivity(intent)
                    }
                    .setCancelable(false)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}