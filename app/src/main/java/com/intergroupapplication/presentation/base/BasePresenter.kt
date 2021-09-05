package com.intergroupapplication.presentation.base

import io.reactivex.disposables.CompositeDisposable
import moxy.MvpPresenter
import moxy.MvpView
import javax.inject.Inject

abstract class BasePresenter<View : MvpView> : MvpPresenter<View>() {

    companion object {
        const val POST_CREATED = 1
        const val POST_NOT_CREATED = 2
        const val GROUP_CREATED = 5
        const val GROUP_NOT_CREATED = 6
    }

    @Inject
    protected lateinit var compositeDisposable: CompositeDisposable

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}
