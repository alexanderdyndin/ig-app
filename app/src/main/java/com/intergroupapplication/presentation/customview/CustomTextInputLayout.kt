package com.intergroupapplication.presentation.customview

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import com.intergroupapplication.R


/**
 * Created by abakarmagomedov on 13/08/2018 at project InterGroupApplication.
 */
class CustomTextInputLayout : TextInputLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        clearEditTextColorfilter()
    }

    override fun setError(@Nullable error: CharSequence?) {
        super.setError(error)
        if (error != null) {
            editText?.background = ContextCompat.getDrawable(context, R.drawable.error_edit_text_drawable)
            editText?.setTextColor(ContextCompat.getColor(context, R.color.errorColor))
        } else {
            clearEditTextColorfilter()
        }
    }

    private fun clearEditTextColorfilter() {
        val editText = editText
        if (editText != null) {
            //val background_auth = editText.background_auth
            //background_auth?.clearColorFilter()
            editText.background = ContextCompat.getDrawable(context, R.drawable.edit_text_drawable)
            editText.setTextColor(ContextCompat.getColor(context, R.color.whiteTextColor))
        }
    }
}
