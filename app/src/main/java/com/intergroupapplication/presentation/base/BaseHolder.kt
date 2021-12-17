package com.intergroupapplication.presentation.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseHolder<T>(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun onBind(data: T)
}
