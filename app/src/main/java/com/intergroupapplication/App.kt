package com.intergroupapplication

import android.annotation.SuppressLint
import android.content.Context
import androidx.multidex.MultiDex
import com.intergroupapplication.data.db.IgDatabase
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
        initializers.forEach { it.initialize(this) }
        if (BuildConfig.DEBUG) {
            //AndroidDevMetrics.initWith(this)
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(BaseCrashlyticsTree())
        }

        // todo db
        val db = IgDatabase.getInstance(applicationContext)
//        val dao = db.groupDao()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
            DaggerAppComponent.builder().create(this)

}
