package com.intergroupapplication.presentation.base

/**
 * Created by abakarmagomedov on 08/08/2018 at project InterGroupApplication.
 */

/**
 *Interface to provide your current view to work with
 */
interface ViewProvider {
    /**
     * Provide view instance, better to have WeakReference to your view
     */
    fun provideCurrentView(): Any?

    /**
     * Clear reference to view to prevent mem leak
     */
    fun clear()
}
