package com.intergroupapplication.presentation.feature.creategroup.di

import android.content.Context
import com.intergroupapplication.data.mapper.CreateGroupMapper
import com.intergroupapplication.data.mapper.group.GroupMapper
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.repository.PhotoRepository
import com.intergroupapplication.data.service.CreateGroupService
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.domain.gateway.AwsUploadingGateway
import com.intergroupapplication.domain.gateway.CreateGroupGateway
import com.intergroupapplication.domain.gateway.PhotoGateway
import com.intergroupapplication.presentation.base.FrescoImageLoader
import com.intergroupapplication.presentation.base.ImageLoader
import com.intergroupapplication.presentation.base.ImageUploader
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.delegate.ImageUploadingDelegate
import com.intergroupapplication.presentation.feature.creategroup.view.CreateGroupFragment
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.provider.DialogProvider
import com.intergroupapplication.presentation.manager.ToastManager
import com.mobsandgeeks.saripaar.Validator
import com.yalantis.ucrop.UCrop
import dagger.Module
import dagger.Provides


@Module
class CreateGroupViewModule {

    @PerFragment
    @Provides
    fun provideValidator(fragment: CreateGroupFragment): Validator =
            Validator(fragment).apply { setValidationListener(fragment) }

    @PerFragment
    @Provides
    fun providePhotoGateway(fragment: CreateGroupFragment, cropOptions: UCrop.Options,
                            api: AppApi, awsUploadingGateway: AwsUploadingGateway): PhotoGateway =
            PhotoRepository(fragment, cropOptions, api, awsUploadingGateway)


    @PerFragment
    @Provides
    fun provideFrescoImageLoader(context: Context): ImageLoader =
            FrescoImageLoader(context)

    @PerFragment
    @Provides
    fun provideImageUploadingDelegate(photoGateway: PhotoGateway): ImageUploader =
            ImageUploadingDelegate(photoGateway)


    @PerFragment
    @Provides
    fun provideCreateGroupGateway(
        api: AppApi, createGroupMapper: CreateGroupMapper,
        groupMapper: GroupMapper
    ): CreateGroupGateway {
        return CreateGroupService(api, createGroupMapper, groupMapper)
    }

    @PerFragment
    @Provides
    fun provideImageLoadingDelegate(imageLoader: ImageLoader): ImageLoadingDelegate =
            ImageLoadingDelegate(imageLoader)


    @PerFragment
    @Provides
    fun provideDialogManager(fragment: CreateGroupFragment): DialogManager =
            DialogManager(fragment.requireActivity().supportFragmentManager)


    @PerFragment
    @Provides
    fun dialogDelegate(dialogManager: DialogManager, dialogProvider: DialogProvider, toastManager: ToastManager,
                       context: Context)
            : DialogDelegate =
            DialogDelegate(dialogManager, dialogProvider, toastManager, context)



}
