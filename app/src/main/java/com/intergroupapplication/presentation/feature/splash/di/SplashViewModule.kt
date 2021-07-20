package com.intergroupapplication.presentation.feature.splash.di

import android.content.Context
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.feature.splash.SplashFragment
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.provider.DialogProvider
import com.intergroupapplication.presentation.manager.ToastManager
import dagger.Module
import dagger.Provides


/**
 * Created by abakarmagomedov on 06/08/2018 at project InterGroupApplication.
 */

@Module
class SplashViewModule {

    @PerFragment
    @Provides
    fun provideDialogManager(fragment: SplashFragment): DialogManager =
            DialogManager(fragment.requireActivity().supportFragmentManager)


    @PerFragment
    @Provides
    fun dialogDelegate(dialogManager: DialogManager, dialogProvider: DialogProvider, toastManager: ToastManager,
                       context: Context)
            : DialogDelegate = DialogDelegate(dialogManager, dialogProvider, toastManager, context)


}
