package com.intergroupapplication.presentation.feature.mainActivity.di

import com.appodeal.ads.Appodeal
import com.intergroupapplication.BuildConfig
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.di.scope.PerActivity
import com.intergroupapplication.initializators.InitializerLocal
import com.intergroupapplication.presentation.feature.mainActivity.view.MainActivity
import dagger.Module
import dagger.Provides


@Module
class MainActivityViewModule {

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
}
