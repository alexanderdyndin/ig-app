package com.intergroupapplication.presentation.feature.creategroup.di

import android.content.Context
import com.intergroupapplication.data.mapper.CreateGroupMapper
import com.intergroupapplication.data.mapper.GroupMapper
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.repository.PhotoRepository
import com.intergroupapplication.data.service.CreateGroupService
import com.intergroupapplication.di.scope.PerActivity
import com.intergroupapplication.domain.gateway.AwsUploadingGateway
import com.intergroupapplication.domain.gateway.CreateGroupGateway
import com.intergroupapplication.domain.gateway.PhotoGateway
import com.intergroupapplication.presentation.base.FrescoImageLoader
import com.intergroupapplication.presentation.base.ImageLoader
import com.intergroupapplication.presentation.base.ImageUploader
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.delegate.ImageUploadingDelegate
import com.intergroupapplication.presentation.feature.creategroup.view.CreateGroupActivity
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.manager.DialogProvider
import com.intergroupapplication.presentation.manager.ToastManager
import com.mobsandgeeks.saripaar.Validator
import com.yalantis.ucrop.UCrop
import dagger.Module
import dagger.Provides
import ru.terrakok.cicerone.android.support.SupportAppNavigator

@Module
class CreateGroupViewModule {

    @PerActivity
    @Provides
    fun provideValidator(activity: CreateGroupActivity): Validator =
            Validator(activity).apply { setValidationListener(activity) }

    @PerActivity
    @Provides
    fun providePhotoGateway(activity: CreateGroupActivity, cropOptions: UCrop.Options,
                            api: AppApi, awsUploadingGateway: AwsUploadingGateway): PhotoGateway =
            PhotoRepository(activity, cropOptions, api, awsUploadingGateway)


    @PerActivity
    @Provides
    fun provideFrescoImageLoader(activity: CreateGroupActivity): ImageLoader =
            FrescoImageLoader(activity)

    @PerActivity
    @Provides
    fun provideImageUploadingDelegate(photoGateway: PhotoGateway): ImageUploader =
            ImageUploadingDelegate(photoGateway)


    @PerActivity
    @Provides
    fun provideCreateGroupGateway(api: AppApi, createGroupMapper: CreateGroupMapper,
                                  groupMapper: GroupMapper): CreateGroupGateway {
        return CreateGroupService(api, createGroupMapper, groupMapper)
    }

    @PerActivity
    @Provides
    fun provideImageLoadingDelegate(imageLoader: ImageLoader): ImageLoadingDelegate =
            ImageLoadingDelegate(imageLoader)


    @PerActivity
    @Provides
    fun provideDialogManager(activity: CreateGroupActivity): DialogManager =
            DialogManager(activity.supportFragmentManager)


    @PerActivity
    @Provides
    fun dialogDelegate(dialogManager: DialogManager, dialogProvider: DialogProvider, toastManager: ToastManager,
                       context: Context)
            : DialogDelegate =
            DialogDelegate(dialogManager, dialogProvider, toastManager, context)


    @PerActivity
    @Provides
    fun provideSupportAppNavigator(activity: CreateGroupActivity): SupportAppNavigator =
            SupportAppNavigator(activity, 0)

}
