package com.intergroupapplication.presentation.feature.createuserprofile.di

import android.content.Context
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.repository.PhotoRepository
import com.intergroupapplication.di.qualifier.UserProfileHandler
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.domain.gateway.AwsUploadingGateway
import com.intergroupapplication.domain.gateway.PhotoGateway
import com.intergroupapplication.presentation.base.FrescoImageLoader
import com.intergroupapplication.presentation.base.ImageLoader
import com.intergroupapplication.presentation.base.ImageUploader
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.delegate.ImageUploadingDelegate
import com.intergroupapplication.presentation.feature.createuserprofile.view.CreateUserProfileFragment
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.provider.DialogProvider
import com.mobsandgeeks.saripaar.Validator
import com.workable.errorhandler.ErrorHandler
import com.yalantis.ucrop.UCrop
import dagger.Module
import dagger.Provides
import java.util.*

@Module
class CreateUserProfileViewModule {

    @PerFragment
    @Provides
    fun providePhotoGateway(
        fragment: CreateUserProfileFragment, cropOptions: UCrop.Options,
        api: AppApi, awsUploadingGateway: AwsUploadingGateway
    ): PhotoGateway =
        PhotoRepository(fragment, cropOptions, api, awsUploadingGateway)


    @PerFragment
    @Provides
    fun provideFrescoImageLoader(): ImageLoader =
        FrescoImageLoader()

    @PerFragment
    @Provides
    fun provideUploadingDelegate(photoGateway: PhotoGateway): ImageUploader =
        ImageUploadingDelegate(photoGateway)


    @PerFragment
    @Provides
    fun provideImageLoadingDelegate(imageLoader: ImageLoader): ImageLoadingDelegate =
        ImageLoadingDelegate(imageLoader)


    @PerFragment
    @Provides
    fun provideDialogManager(fragment: CreateUserProfileFragment): DialogManager =
        DialogManager(fragment.requireActivity().supportFragmentManager)


    @PerFragment
    @Provides
    fun dialogDelegate(
        dialogManager: DialogManager, dialogProvider: DialogProvider,
        context: Context
    )
            : DialogDelegate =
        DialogDelegate(dialogManager, dialogProvider, context)


    @PerFragment
    @Provides
    fun provideCalendar(): Calendar =
        Calendar.getInstance(Locale.getDefault())


    @PerFragment
    @Provides
    fun provideValidator(fragment: CreateUserProfileFragment): Validator =
        Validator(fragment).apply { setValidationListener(fragment) }

    @PerFragment
    @Provides
    @UserProfileHandler
    fun errorHandler(): ErrorHandler = ErrorHandler.create()
}
