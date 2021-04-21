package com.intergroupapplication.presentation.feature.commentsdetails.di

import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.feature.commentsdetails.adapter.MediaAdapter
import dagger.Module
import dagger.Provides

@Module
class BottomSheetViewModule {

    @PerFragment
    @Provides
    fun provideMediaAdapter(imageLoadingDelegate: ImageLoadingDelegate): MediaAdapter {
        return MediaAdapter(imageLoadingDelegate)
    }
}