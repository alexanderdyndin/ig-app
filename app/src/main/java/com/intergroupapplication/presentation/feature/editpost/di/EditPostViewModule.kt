package com.intergroupapplication.presentation.feature.editpost.di

import android.content.Context
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.repository.PhotoRepository
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.domain.gateway.AwsUploadingGateway
import com.intergroupapplication.domain.gateway.PhotoGateway
import com.intergroupapplication.presentation.base.FrescoImageLoader
import com.intergroupapplication.presentation.base.ImageLoader
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.feature.editpost.view.EditPostFragment
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.manager.DialogProvider
import com.intergroupapplication.presentation.manager.ToastManager
import com.yalantis.ucrop.UCrop
import dagger.Module
import dagger.Provides

@Module
class EditPostViewModule {

    @PerFragment
    @Provides
    fun provideDialogManager(activity: EditPostFragment): DialogManager =
            DialogManager(activity.requireActivity().supportFragmentManager)

    @PerFragment
    @Provides
    fun providePhotoGateway(activity: EditPostFragment, cropOptions: UCrop.Options,
                            api: AppApi, awsUploadingGateway: AwsUploadingGateway): PhotoGateway =
            PhotoRepository(activity.requireActivity(), cropOptions, api, awsUploadingGateway)

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
    fun dialogDelegate(dialogManager: DialogManager, dialogProvider: DialogProvider, toastManager: ToastManager,
                       context: Context)
            : DialogDelegate =
            DialogDelegate(dialogManager, dialogProvider, toastManager, context)
}