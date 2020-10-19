package com.intergroupapplication.presentation.feature.createpost.di

import android.content.Context
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.repository.PhotoRepository
import com.intergroupapplication.di.scope.PerActivity
import com.intergroupapplication.domain.gateway.AwsUploadingGateway
import com.intergroupapplication.domain.gateway.PhotoGateway
import com.intergroupapplication.presentation.base.FrescoImageLoader
import com.intergroupapplication.presentation.base.ImageLoader
import com.intergroupapplication.presentation.base.ImageUploader
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.delegate.ImageUploadingDelegate
import com.intergroupapplication.presentation.feature.createpost.view.CreatePostActivity
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.manager.DialogProvider
import com.intergroupapplication.presentation.manager.ToastManager
import com.yalantis.ucrop.UCrop
import dagger.Module
import dagger.Provides
import ru.terrakok.cicerone.android.support.SupportAppNavigator

@Module
class CreatePostViewModule {

    @PerActivity
    @Provides
    fun provideDialogManager(activity: CreatePostActivity): DialogManager =
            DialogManager(activity.supportFragmentManager)

    @PerActivity
    @Provides
    fun providePhotoGateway(activity: CreatePostActivity, cropOptions: UCrop.Options,
                            api: AppApi, awsUploadingGateway: AwsUploadingGateway): PhotoGateway =
            PhotoRepository(activity, cropOptions, api, awsUploadingGateway)

    @PerActivity
    @Provides
    fun provideFrescoImageLoader(activity: CreatePostActivity): ImageLoader =
            FrescoImageLoader(activity)


    @PerActivity
    @Provides
    fun provideImageLoadingDelegate(imageLoader: ImageLoader): ImageLoadingDelegate =
            ImageLoadingDelegate(imageLoader)

    @PerActivity
    @Provides
    fun provideImageUploadingDelegate(photoGateway: PhotoGateway): ImageUploader =
            ImageUploadingDelegate(photoGateway)

    @PerActivity
    @Provides
    fun dialogDelegate(dialogManager: DialogManager, dialogProvider: DialogProvider, toastManager: ToastManager,
                       context: Context)
            : DialogDelegate =
            DialogDelegate(dialogManager, dialogProvider, toastManager, context)


    @PerActivity
    @Provides
    fun provideSupportAppNavigator(activity: CreatePostActivity): SupportAppNavigator =
            SupportAppNavigator(activity, 0)

}