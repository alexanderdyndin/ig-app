package com.intergroupapplication.presentation.base

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable

open class BaseViewModel: ViewModel() {

    protected val viewModelDisposable = CompositeDisposable()

    override fun onCleared() {
        super.onCleared()
        viewModelDisposable.clear()
    }
}