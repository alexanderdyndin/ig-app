package com.intergroupapplication.data.network

/**
 * Created by abakarmagomedov on 24/08/2018 at project InterGroupApplication.
 */
interface ErrorAdapter {
    fun adapt(throwable: Throwable): Throwable
}
