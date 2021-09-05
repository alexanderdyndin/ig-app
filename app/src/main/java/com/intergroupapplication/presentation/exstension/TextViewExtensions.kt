package com.intergroupapplication.presentation.exstension

import android.widget.TextView
import androidx.core.content.ContextCompat
import com.intergroupapplication.R
import com.klinker.android.link_builder.Link
import com.klinker.android.link_builder.LinkBuilder

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

fun TextView.changeActivatedTextView(thisActivated:Boolean, view1:TextView, view2:TextView,
                                        view3:TextView){
    this.changeActivated(thisActivated, view1, view2, view3)
    this.setTextColor(if (thisActivated)resources.getColor(R.color.pingForButton, null)
    else resources.getColor(R.color.colorAccent, null) )
    view1.setTextColor(resources.getColor(R.color.colorAccent, null))
    view2.setTextColor(resources.getColor(R.color.colorAccent, null))
    view3.setTextColor(resources.getColor(R.color.colorAccent, null))
}