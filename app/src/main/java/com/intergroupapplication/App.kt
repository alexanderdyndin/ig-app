package com.intergroupapplication

import android.annotation.SuppressLint
import android.content.Context
import androidx.multidex.MultiDex
import com.facebook.common.memory.MemoryTrimType
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.intergroupapplication.di.DaggerAppComponent
import com.intergroupapplication.initializators.Initializer
import com.intergroupapplication.presentation.base.BaseCrashlyticsTree
import com.intergroupapplication.presentation.base.FrescoMemoryTrimmableRegistry
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import timber.log.Timber
import javax.inject.Inject


class App : DaggerApplication() {

    @Inject
    lateinit var initializers: Set<@JvmSuppressWildcards Initializer>

    @Inject
    lateinit var frescoMemoryTrimmableRegistry: FrescoMemoryTrimmableRegistry

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
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        when (level) {
            TRIM_MEMORY_UI_HIDDEN -> {
                frescoMemoryTrimmableRegistry.trim(MemoryTrimType.OnAppBackgrounded)
                Timber.d("OnAppBackgrounded - level = %s", level)
            }
            TRIM_MEMORY_RUNNING_MODERATE, TRIM_MEMORY_RUNNING_LOW, TRIM_MEMORY_RUNNING_CRITICAL -> {
                frescoMemoryTrimmableRegistry.trim(MemoryTrimType.OnCloseToDalvikHeapLimit)
                //clearMemoryCaches()
                Timber.d("OnCloseToDalvikHeapLimit - level = $level")
            }
            TRIM_MEMORY_BACKGROUND, TRIM_MEMORY_MODERATE, TRIM_MEMORY_COMPLETE -> {
                frescoMemoryTrimmableRegistry.trim(MemoryTrimType.OnSystemLowMemoryWhileAppInForeground)
                Timber.d("OnSystemLowMemoryWhileAppInForeground - level = %s", level)
            }
            else -> Timber.d("default - level = %s", level)
        }
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
            DaggerAppComponent.builder().create(this)

}
