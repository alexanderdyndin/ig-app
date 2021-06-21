package com.intergroupapplication

import android.annotation.SuppressLint
import com.intergroupapplication.di.DaggerAppComponent
import com.intergroupapplication.initializators.Initializer
import com.intergroupapplication.presentation.base.BaseCrashlyticsTree
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import timber.log.Timber
import javax.inject.Inject


class App : DaggerApplication() {

    @Inject
    lateinit var initializers: Set<@JvmSuppressWildcards Initializer>

    @SuppressLint("CheckResult")
    override fun onCreate() {
        super.onCreate()
        initializers.forEach {
            it.initialize(this)
        }
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(BaseCrashlyticsTree())
        }
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
            DaggerAppComponent.builder().create(this)

}
