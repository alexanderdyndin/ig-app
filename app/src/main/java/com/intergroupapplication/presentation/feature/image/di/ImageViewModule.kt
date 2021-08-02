package com.intergroupapplication.presentation.feature.image.di

import android.content.Context
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.presentation.base.FrescoImageLoader
import com.intergroupapplication.presentation.base.ImageLoader
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.feature.image.view.ImageFragment
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.manager.ToastManager
import com.intergroupapplication.presentation.provider.DialogProvider
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


    @PerFragment
    @Provides
    fun provideDialogManager(fragment: ImageFragment): DialogManager =
        DialogManager(fragment.requireActivity().supportFragmentManager)


    @PerFragment
    @Provides
    fun dialogDelegate(
        dialogManager: DialogManager, dialogProvider: DialogProvider, toastManager: ToastManager,
        context: Context
    )
            : DialogDelegate =
        DialogDelegate(dialogManager, dialogProvider, toastManager, context)
}
