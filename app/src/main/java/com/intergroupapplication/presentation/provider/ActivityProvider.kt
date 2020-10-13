package com.intergroupapplication.presentation.provider

import androidx.appcompat.app.AppCompatActivity
import com.intergroupapplication.presentation.base.ViewProvider
import java.lang.ref.WeakReference

/**
 * Created by abakarmagomedov on 08/08/2018 at project InterGroupApplication.
 */
class ActivityProvider(private val acitvity: WeakReference<AppCompatActivity>) : ViewProvider {

    override fun provideCurrentView(): Any? = acitvity.get()

    override fun clear() = acitvity.clear()
}
