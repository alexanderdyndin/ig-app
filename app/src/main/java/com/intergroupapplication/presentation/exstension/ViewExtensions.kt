package com.intergroupapplication.presentation.exstension

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.LayoutRes
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

/**
 * Created by abakarmagomedov on 09/08/2018 at project InterGroupApplication.
 */

fun View.isVisible(): Boolean = this.visibility == View.VISIBLE

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.hide() {
    this.visibility = View.INVISIBLE
}

fun View.hideKeyboard() {
    requestFocus()
    val inputManager = this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    inputManager?.hideSoftInputFromWindow(this.windowToken, 0)
}

fun View.showKeyBoard() {
    requestFocus()
    val inputManager = this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    inputManager?.showSoftInput(this, 0)
}

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun View.clicks(time: Long = 600): Observable<Any> {
    return RxView.clicks(this).throttleFirst(time, TimeUnit.MILLISECONDS)
}
fun View.activated(isActivated:Boolean){
    this.isActivated = isActivated
}

fun View.changeActivated(thisActivated:Boolean, vararg views:View){
    this.activated(thisActivated)
    views.forEach { view->
        view.activated(false)
    }
}
