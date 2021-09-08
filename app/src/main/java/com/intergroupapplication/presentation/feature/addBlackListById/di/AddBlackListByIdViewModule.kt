package com.intergroupapplication.presentation.feature.addBlackListById.di

import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.presentation.base.FrescoImageLoader
import com.intergroupapplication.presentation.base.ImageLoader
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.feature.addBlackListById.adapter.AddUserBlackListAdapter
import dagger.Module
import dagger.Provides

@Module
class AddBlackListByIdViewModule {

    @PerFragment
    @Provides
    fun provideFrescoImageLoader(): ImageLoader =
        FrescoImageLoader()

    @PerFragment
    @Provides
    fun provideImageLoadingDelegate(imageLoader: ImageLoader): ImageLoadingDelegate =
        ImageLoadingDelegate(imageLoader)


    @PerFragment
    @Provides
    fun provideAddUserBlackListAdapter(imageLoadingDelegate: ImageLoadingDelegate): AddUserBlackListAdapter {
        return AddUserBlackListAdapter(imageLoadingDelegate)
    }
}
