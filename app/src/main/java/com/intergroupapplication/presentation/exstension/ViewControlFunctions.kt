package com.intergroupapplication.presentation.exstension

import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.intergroupapplication.R

fun setViewErrorState(editText: AppCompatEditText) {
    editText.setTextColor(ContextCompat.getColor(editText.context, R.color.errorColor))
}


