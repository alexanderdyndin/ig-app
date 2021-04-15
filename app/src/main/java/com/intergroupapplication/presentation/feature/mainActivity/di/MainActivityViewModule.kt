package com.intergroupapplication.presentation.feature.mainActivity.di

import android.app.Application
import android.content.Context
import com.appodeal.ads.Appodeal
import com.appodeal.ads.UserSettings
import com.intergroupapplication.BuildConfig
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.repository.PhotoRepository
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.di.scope.PerActivity
import com.intergroupapplication.domain.gateway.AwsUploadingGateway
import com.intergroupapplication.domain.gateway.PhotoGateway
import com.intergroupapplication.initializators.Initializer
import com.intergroupapplication.initializators.InitializerLocal
import com.intergroupapplication.presentation.base.FrescoImageLoader
import com.intergroupapplication.presentation.base.ImageLoader
import com.intergroupapplication.presentation.base.ImageUploader
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.delegate.ImageUploadingDelegate
import com.intergroupapplication.presentation.feature.mainActivity.view.MainActivity
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
class MainActivityViewModule {

    @PerActivity
    @Provides
    fun provideAdMobInitializer(userSession: UserSession, activity: MainActivity): InitializerLocal = object : InitializerLocal {
        override fun initialize() {
            Appodeal.initialize(activity, BuildConfig.APPODEAL_APP_KEY, Appodeal.NATIVE, userSession.isAcceptTerms())
            //Appodeal.setTesting(true)
            userSession.user?.let {
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).parse(it.birthday)
                val c = Calendar.getInstance()
                val year = c.get(Calendar.YEAR)
                Appodeal.setUserAge(year - date.year)
                val gender = when (it.gender) {
                    "male" -> UserSettings.Gender.MALE
                    "female" -> UserSettings.Gender.FEMALE
                    else -> UserSettings.Gender.OTHER
                }
                Appodeal.setUserGender(gender)
                Appodeal.setUserId(it.id)
            }
            Appodeal.cache(activity, Appodeal.NATIVE, 1)
        }
    }
}
