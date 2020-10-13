package com.intergroupapplication.di.module

import com.intergroupapplication.di.scope.PerApplication
import com.intergroupapplication.presentation.listeners.RightDrawableListener
import dagger.Module
import dagger.Provides

/**
 * Created by abakarmagomedov on 22/08/2018 at project InterGroupApplication.
 */
@Module
class ListenersModule {

    @PerApplication
    @Provides
    fun provideRightDrawableListener(): RightDrawableListener = RightDrawableListener()
}
