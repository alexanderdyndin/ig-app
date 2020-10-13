package com.intergroupapplication.presentation.base.adapter

/**
 * Created by abakarmagomedov on 16/10/2018 at project InterGroupApplication.
 */
interface PagingAdapter {
    fun addLoading()
    fun removeLoading()
    fun addError()
    fun removeError()
    fun itemCount(): Int
}