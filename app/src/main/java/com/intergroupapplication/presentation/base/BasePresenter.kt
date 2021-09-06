package com.intergroupapplication.presentation.base

import io.reactivex.disposables.CompositeDisposable
import moxy.MvpPresenter
import moxy.MvpView
import javax.inject.Inject

abstract class BasePresenter<View : MvpView> : MvpPresenter<View>() {

    @Inject
    protected lateinit var compositeDisposable: CompositeDisposable

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}
