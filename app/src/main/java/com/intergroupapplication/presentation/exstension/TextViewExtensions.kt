package com.intergroupapplication.presentation.exstension

import android.content.Context
import androidx.core.content.ContextCompat
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import com.intergroupapplication.R
import com.jakewharton.rxbinding2.view.RxView
import com.klinker.android.link_builder.Link
import com.klinker.android.link_builder.LinkBuilder
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

/**
 * Created by abakarmagomedov on 01/08/2018 at project InterGroupApplication.
 */

/**
 * @param  action The action performed when link clicked
 */

private const val HIGHLIGHT_ALPHA = 0.4f

fun TextView.setLinkClickable(action: () -> (Unit)) {
    val link = Link(this.text.toString())
            .setTextColor(ContextCompat.getColor(this.context, R.color.linkGrayTextColor))
            .setHighlightAlpha(HIGHLIGHT_ALPHA)
            .setUnderlined(false)
            .setOnClickListener {
                action()
            }
    LinkBuilder.on(this)
            .addLink(link)
            .build()
}

fun TextView.showKeyboard() {
    requestFocus()
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}