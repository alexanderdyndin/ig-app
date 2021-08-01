package com.intergroupapplication.presentation.exstension

import androidx.core.content.ContextCompat
import androidx.appcompat.widget.AppCompatEditText
import com.intergroupapplication.R

fun setViewErrorState(editText: AppCompatEditText) {
    editText.setTextColor(ContextCompat.getColor(editText.context, R.color.errorColor))
}


