package com.intergroupapplication.di.module

import com.intergroupapplication.di.qualifier.ComputationScheduler
import com.intergroupapplication.di.qualifier.IoScheduler
import com.intergroupapplication.di.qualifier.MainScheduler
import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@Module
class RxModule {

    @Provides
    @IoScheduler
    fun provideSchedulerIO(): Scheduler = Schedulers.io()

    @Provides
    @ComputationScheduler
    fun provideSchedulerComputation(): Scheduler = Schedulers.computation()

    @Provides
    @MainScheduler
    fun provideSchedulerMainThread(): Scheduler = AndroidSchedulers.mainThread()

    @Provides
    fun provideCompositeDisposable() = CompositeDisposable()
}
