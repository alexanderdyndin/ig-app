package com.intergroupapplication.presentation.feature.creategroup.other

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.intergroupapplication.R
import kotlinx.android.synthetic.main.dialog_input.*


class InputDialog(private val title: String): DialogFragment() {

    var changedText: ((text: String) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_input, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        textView2.text = title
        button2.setOnClickListener {
            changedText?.invoke(editTextTextMultiLine.text.toString())
            dialog?.dismiss()
        }
        super.onViewCreated(view, savedInstanceState)
    }

}