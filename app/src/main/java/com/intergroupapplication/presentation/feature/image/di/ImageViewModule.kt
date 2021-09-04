package com.intergroupapplication.presentation.feature.image.di

import android.content.Context
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.presentation.base.FrescoImageLoader
import com.intergroupapplication.presentation.base.ImageLoader
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import dagger.Module
import dagger.Provides


@Module
class ImageViewModule {

    @PerFragment
    @Provides
    fun provideFrescoImageLoader(context: Context): ImageLoader =
            FrescoImageLoader(context)

    @PerFragment
    @Provides
    fun provideImageLoadingDelegate(imageLoader: ImageLoader): ImageLoadingDelegate =
            ImageLoadingDelegate(imageLoader)


}
