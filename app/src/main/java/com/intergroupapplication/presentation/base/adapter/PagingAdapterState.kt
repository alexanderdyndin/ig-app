package com.intergroupapplication.presentation.base.adapter


interface PagingAdapterState {
    fun addLoading() {}
    fun removeLoading() {}
    fun addError() {}
    fun removeError() {}
    fun getViewType(): InterGroupViewType
}