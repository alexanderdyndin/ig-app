package com.intergroupapplication.presentation.base.adapter

import java.util.*

interface InterGroupAdapterItem<T> {
    fun getViewType(): InterGroupViewType
    fun getItems(): List<T> = Collections.emptyList<T>()
}
