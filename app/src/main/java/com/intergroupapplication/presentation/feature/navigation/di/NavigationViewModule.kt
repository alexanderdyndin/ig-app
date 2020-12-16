package com.intergroupapplication.presentation.feature.navigation.di

import android.app.Activity
import android.app.Application
import android.content.Context
import com.appodeal.ads.Appodeal
import com.appodeal.ads.UserSettings
import com.intergroupapplication.R
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.repository.PhotoRepository
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.di.scope.PerActivity
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.domain.gateway.AwsUploadingGateway
import com.intergroupapplication.domain.gateway.PhotoGateway
import com.intergroupapplication.initializators.Initializer
import com.intergroupapplication.presentation.base.FrescoImageLoader
import com.intergroupapplication.presentation.base.ImageLoader
import com.intergroupapplication.presentation.base.ImageUploader
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.delegate.ImageUploadingDelegate
import com.intergroupapplication.presentation.feature.navigation.view.NavigationActivity
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.manager.DialogProvider
import com.intergroupapplication.presentation.manager.ToastManager
import com.yalantis.ucrop.UCrop
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

import java.text.SimpleDateFormat
import java.util.*

@Module
class NavigationViewModule {

    companion object {
        const val GROUP_ID = "group_id"
    }

    @PerFragment
    @Provides
    fun provideFrescoImageLoader(activity: NavigationActivity): ImageLoader =
            FrescoImageLoader(activity.requireActivity())


    @PerFragment
    @Provides
    fun provideImageLoadingDelegate(imageLoader: ImageLoader): ImageLoadingDelegate =
            ImageLoadingDelegate(imageLoader)

    @PerFragment
    @Provides
    fun providePhotoGateway(activity: NavigationActivity, cropOptions: UCrop.Options,
                            api: AppApi, awsUploadingGateway: AwsUploadingGateway): PhotoGateway =
            PhotoRepository(activity.requireActivity(), cropOptions, api, awsUploadingGateway)

    @PerFragment
    @Provides
    fun provideImageUploader(photoGateway: PhotoGateway): ImageUploader =
            ImageUploadingDelegate(photoGateway)


    @PerFragment
    @Provides
    fun provideDialogManager(activity: NavigationActivity): DialogManager =
            DialogManager(activity.requireActivity().supportFragmentManager)


    @PerFragment
    @Provides
    fun dialogDelegate(dialogManager: DialogManager, dialogProvider: DialogProvider, toastManager: ToastManager,
                       context: Context)
            : DialogDelegate =
            DialogDelegate(dialogManager, dialogProvider, toastManager, context)


//    @PerActivity
//    @Provides
//    fun provideSupportAppNavigator(activity: NavigationActivity): SupportAppNavigator =
//            SupportAppNavigator(activity, R.id.mainContainer)

//    @PerActivity
//    @Provides
//    fun setAppodeal(activity: NavigationActivity, userSession: UserSession) {
//            Appodeal.initialize(activity, "57968e20342bf80c873fd55868f65f7b", Appodeal.NATIVE, userSession.isAcceptTerms())
//            val date = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).parse(userSession.user?.birthday)
//            val c = Calendar.getInstance()
//            val year = c.get(Calendar.YEAR)
//            Appodeal.setUserAge(year - date.year)
//            val gender = when (userSession.user?.gender) {
//                "male" -> UserSettings.Gender.MALE
//                "female" -> UserSettings.Gender.FEMALE
//                else -> UserSettings.Gender.OTHER
//            }
//            Appodeal.setUserGender(gender)
//            Appodeal.cache(activity, Appodeal.NATIVE, 3)
//    }
}
