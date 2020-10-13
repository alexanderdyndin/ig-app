package com.intergroupapplication.presentation.feature.splash.di

import android.content.Context
import com.intergroupapplication.di.scope.PerActivity
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.feature.splash.SplashActivity
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.manager.DialogProvider
import com.intergroupapplication.presentation.manager.ToastManager
import dagger.Module
import dagger.Provides
import ru.terrakok.cicerone.android.support.SupportAppNavigator

/**
 * Created by abakarmagomedov on 06/08/2018 at project InterGroupApplication.
 */

@Module
class SplashViewModule {

    @PerActivity
    @Provides
    fun provideDialogManager(activity: SplashActivity): DialogManager =
            DialogManager(activity.supportFragmentManager)


    @PerActivity
    @Provides
    fun dialogDelegate(dialogManager: DialogManager, dialogProvider: DialogProvider, toastManager: ToastManager,
                       context: Context)
            : DialogDelegate = DialogDelegate(dialogManager, dialogProvider, toastManager, context)


    @PerActivity
    @Provides
    fun provideSupportAppNavigator(activity: SplashActivity): SupportAppNavigator =
            SupportAppNavigator(activity, 0)

}
