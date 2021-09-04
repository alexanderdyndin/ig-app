package com.intergroupapplication.presentation.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.intergroupapplication.di.scope.PerApplication
import javax.inject.Inject
import javax.inject.Provider

@PerApplication
class ViewModelFactory @Inject constructor(
        private val creators: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return creators.getValue(modelClass as Class<ViewModel>).get() as T
    }
}