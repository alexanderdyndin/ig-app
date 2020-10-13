package com.intergroupapplication.presentation.feature.createuserprofile.di

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
import com.intergroupapplication.presentation.feature.createuserprofile.view.CreateUserProfileActivity
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.manager.DialogProvider
import com.intergroupapplication.presentation.manager.ToastManager
import com.mobsandgeeks.saripaar.Validator
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.yalantis.ucrop.UCrop
import dagger.Module
import dagger.Provides
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import java.util.*

@Module
class CreateUserProfileViewModule {

    @PerActivity
    @Provides
    fun providePhotoGateway(activity: CreateUserProfileActivity, cropOptions: UCrop.Options,
                            api: AppApi, awsUploadingGateway: AwsUploadingGateway): PhotoGateway =
            PhotoRepository(activity, cropOptions, api, awsUploadingGateway)


    @PerActivity
    @Provides
    fun provideFrescoImageLoader(activity: CreateUserProfileActivity): ImageLoader =
            FrescoImageLoader(activity)

    @PerActivity
    @Provides
    fun provideUploadingDelegate(photoGateway: PhotoGateway): ImageUploader =
            ImageUploadingDelegate(photoGateway)


    @PerActivity
    @Provides
    fun provideImageLoadingDelegate(imageLoader: ImageLoader): ImageLoadingDelegate =
            ImageLoadingDelegate(imageLoader)


    @PerActivity
    @Provides
    fun provideDialogManager(activity: CreateUserProfileActivity): DialogManager =
            DialogManager(activity.supportFragmentManager)


    @PerActivity
    @Provides
    fun dialogDelegate(dialogManager: DialogManager, dialogProvider: DialogProvider, toastManager: ToastManager,
                       context: Context)
            : DialogDelegate =
            DialogDelegate(dialogManager, dialogProvider, toastManager, context)


    @PerActivity
    @Provides
    fun provideCalendar(): Calendar =
            Calendar.getInstance(Locale.getDefault())


    @PerActivity
    @Provides
    fun provideSupportAppNavigator(activity: CreateUserProfileActivity): SupportAppNavigator =
            SupportAppNavigator(activity, 0)

    @PerActivity
    @Provides
    fun provideValidator(activity: CreateUserProfileActivity): Validator =
            Validator(activity).apply { setValidationListener(activity) }
}
