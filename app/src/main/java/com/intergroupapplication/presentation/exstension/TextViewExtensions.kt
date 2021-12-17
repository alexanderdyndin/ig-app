package com.intergroupapplication.presentation.exstension

import android.widget.TextView
import com.intergroupapplication.R

fun TextView.changeActivatedTextView(
    thisActivated: Boolean, view1: TextView, view2: TextView,
    view3: TextView
) {
    this.changeActivated(thisActivated, view1, view2, view3)
    this.setTextColor(
        if (thisActivated) resources.getColor(R.color.pingForButton, null)
        else resources.getColor(R.color.colorAccent, null)
    )
    view1.setTextColor(resources.getColor(R.color.colorAccent, null))
    view2.setTextColor(resources.getColor(R.color.colorAccent, null))
    view3.setTextColor(resources.getColor(R.color.colorAccent, null))
}
