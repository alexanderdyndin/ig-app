package com.intergroupapplication.presentation.base

/**
 * Created by abakarmagomedov on 19/09/2018 at project InterGroupApplication.
 */
class BasePagingState(val type: Type,
                      val error: Throwable? = null) {

    companion object {
        const val PAGINATION_PAGE_SIZE = 20
    }

    enum class Type {
        NONE, LOADING, ERROR
    }
}