package com.intergroupapplication.presentation.feature.mainActivity.di

import android.content.Context
import com.appodeal.ads.Appodeal
import com.intergroupapplication.BuildConfig
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.di.scope.PerActivity
import com.intergroupapplication.initializators.InitializerLocal
import com.intergroupapplication.presentation.base.FrescoImageLoader
import com.intergroupapplication.presentation.base.ImageLoader
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.feature.mainActivity.view.MainActivity
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.provider.DialogProvider
import dagger.Module
import dagger.Provides


@Module
class MainActivityViewModule {

    @PerActivity
    @Provides
    fun provideDialogManager(activity: MainActivity): DialogManager =
        DialogManager(activity.supportFragmentManager)

    @PerActivity
    @Provides
    fun dialogDelegate(
        dialogManager: DialogManager, dialogProvider: DialogProvider,
        context: Context
    )
            : DialogDelegate =
        DialogDelegate(dialogManager, dialogProvider, context)


    @PerActivity
    @Provides
    fun provideAdMobInitializer(
        userSession: UserSession,
        activity: MainActivity
    ): InitializerLocal = object : InitializerLocal {
        override fun initialize() {
            Appodeal.initialize(
                activity,
                BuildConfig.APPODEAL_APP_KEY,
                Appodeal.NATIVE or Appodeal.BANNER,
                userSession.isAcceptTerms()
            )
            Appodeal.cache(activity, Appodeal.NATIVE, 6)
            Appodeal.cache(activity, Appodeal.BANNER)
            Appodeal.setSmartBanners(true)
        }
    }

    @PerActivity
    @Provides
    fun provideFrescoImageLoader(): ImageLoader =
        FrescoImageLoader()


    @PerActivity
    @Provides
    fun provideImageLoadingDelegate(imageLoader: ImageLoader): ImageLoadingDelegate =
        ImageLoadingDelegate(imageLoader)
}
