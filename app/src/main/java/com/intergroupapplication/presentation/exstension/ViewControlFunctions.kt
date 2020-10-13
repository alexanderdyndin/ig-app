package com.intergroupapplication.presentation.exstension

import android.content.Context
import androidx.core.content.ContextCompat
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.textfield.TextInputLayout
import com.intergroupapplication.R
import com.intergroupapplication.presentation.customview.CustomTextInputLayout

/**
 * Created by abakarmagomedov on 06/08/2018 at project InterGroupApplication.
 */

fun manageVisibility(transformView: View, targetView: View, progressView: ProgressBar,
                     vararg focusViews: EditText,
                     targetVisibilityListener: () -> Unit) {
    if (optimizeVisibilityChanging(targetView, transformView, focusViews)) {
        return
    }
    if (isViewsInvisible(targetView, progressView, transformView)) {
        targetView.show()
        makeInvisible(transformView)
        targetVisibilityListener.invoke()
    }

}

fun manageVisibilityWithToggle(transformView: View, targetView: View, progressView: ProgressBar,
                               radioGroup: RadioGroup,
                               vararg focusViews: TextView, targetVisibilityListener: () -> Unit) {
    if (radioGroup.checkedRadioButtonId == -1) {
        makeInvisible(targetView)
        return
    }
    if (optimizeVisibilityChanging(targetView, transformView, focusViews)) {
        return
    }
    if (isViewsInvisible(targetView, progressView, transformView)) {
        targetView.show()
        makeInvisible(transformView)
        targetVisibilityListener.invoke()
    }
}

fun makeInvisible(vararg views: View) {
    for (view in views) {
        view.hide()
    }
}

fun makeVisible(vararg views: View, targetVisibilityListener: () -> Unit) {
    for (view in views) {
        view.show()
    }
    targetVisibilityListener.invoke()
}

fun setViewErrorStatePassword(editText: AppCompatEditText, editTextInput: TextInputLayout) {
    editTextInput.setPasswordVisibilityToggleDrawable(R.drawable.toggle_pass_red)
    editText.setTextColor(ContextCompat.getColor(editText.context, R.color.errorColor))
}

fun clearViewErrorStatePassword(editText: AppCompatEditText, editTextInput: TextInputLayout) {
    editText.background = ContextCompat.getDrawable(editText.context, R.drawable.edit_text_drawable)
    editTextInput.setPasswordVisibilityToggleDrawable(R.drawable.toggle_pass)
    editText.setTextColor(ContextCompat.getColor(editText.context, R.color.whiteTextColor))
    editTextInput.error = null
}

fun setViewErrorStateMail(editText: AppCompatEditText) {
    editText.setCompoundDrawablesWithIntrinsicBounds(null, null,
            (ContextCompat.getDrawable(editText.context, R.drawable.ic_clear_red)), null)
    editText.setTextColor(ContextCompat.getColor(editText.context, R.color.errorColor))
}

fun setViewErrorState(editText: AppCompatEditText) {
    editText.setTextColor(ContextCompat.getColor(editText.context, R.color.errorColor))
}

fun clearViewErrorStateMail(editText: AppCompatEditText, editTextInput: TextInputLayout) {
    editText.setCompoundDrawablesWithIntrinsicBounds(null, null,
            (ContextCompat.getDrawable(editText.context, R.drawable.ic_clear_white)), null)
    editText.background = ContextCompat.getDrawable(editText.context, R.drawable.edit_text_drawable)
    editText.setTextColor(ContextCompat.getColor(editText.context, R.color.whiteTextColor))
    editTextInput.error = null
}

fun changeMailInputIcon(inputLayout: CustomTextInputLayout?, editText: AppCompatEditText) {
    inputLayout?.let { clearViewErrorStateMail(editText, it) }
    editText.text?.let { text ->
        if (!text.isEmpty()) {
            setIconToEditText(editText, R.drawable.ic_clear_white, R.drawable.ic_clear_white_disabled)
        } else {
            setIconToEditText(editText, R.drawable.ic_email, R.drawable.ic_email_disabled)
        }
    }
}

fun setFocusChangedListenerToEmail(editText: AppCompatEditText) {
    editText.setOnFocusChangeListener { v, hasFocus ->
        editText.text?.let { text ->
            if (!text.isEmpty()) {
                setIconToEditText(editText, R.drawable.ic_clear_white, R.drawable.ic_clear_white_disabled)
            } else {
                setIconToEditText(editText, R.drawable.ic_email, R.drawable.ic_email_disabled)
            }
        }
    }
}

fun setFocusChangedListenerToPassword(editText: AppCompatEditText, editTextInput: TextInputLayout) {
    editText.setOnFocusChangeListener { v, hasFocus ->
        if (hasFocus) {
            editTextInput.setPasswordVisibilityToggleDrawable(R.drawable.toggle_pass)
        } else {
            if (editTextInput.error == null) {
                editTextInput.setPasswordVisibilityToggleDrawable(R.drawable.toggle_pass_disabled)
            } else {
                editTextInput.setPasswordVisibilityToggleDrawable(R.drawable.toggle_pass_red)
            }
        }
    }
}

fun setIconToEditText(editText: AppCompatEditText, focusedIcon: Int, notFocusedIcon: Int) {
    if (editText.isFocused) {
        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(editText.context,
                focusedIcon), null)
    } else {
        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(editText.context,
                notFocusedIcon), null)
    }
}

fun clearPasswordInputState(inputLayout: CustomTextInputLayout?, editText: AppCompatEditText) {
    inputLayout?.error?.let {
        clearViewErrorStatePassword(editText, inputLayout)
    }
}

fun startAlphaAnimation(v: View, duration: Long, visibility: Int) {
    val alphaAnimation = if (visibility == View.VISIBLE)
        AlphaAnimation(0f, 1f)
    else
        AlphaAnimation(1f, 0f)

    alphaAnimation.duration = duration
    alphaAnimation.fillAfter = true
    v.startAnimation(alphaAnimation)
}

/**
 * This method is used for determine as soon as possible if we can just stop views visibility checking
 * @param targetView - main view, transformation applied on
 * @param transformView - view which is end state of transformation from targetView
 * @param focusViews - inputs which states allow us to know when to start transformation
 */
fun optimizeVisibilityChanging(targetView: View, transformView: View,
                               focusViews: Array<out TextView>): Boolean {
    for (editText in focusViews) {
        if (isViewsInvisible(targetView, transformView) && isEditTextEmpty(editText)) {
            return true
        }
        if (isEditTextEmpty(editText)) {
            makeInvisible(targetView, transformView)
            return true
        }
    }
    return false
}

private fun isEditTextEmpty(editText: TextView): Boolean = editText.text.trim().isEmpty()

private fun isViewInvisible(view: View): Boolean = !view.isVisible()

private fun isViewsInvisible(vararg views: View): Boolean {
    for (view in views) {
        if (!isViewInvisible(view)) {
            return false
        }
    }
    return true
}

