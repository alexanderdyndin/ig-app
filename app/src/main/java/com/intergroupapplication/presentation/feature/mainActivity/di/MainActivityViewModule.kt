package com.intergroupapplication.presentation.feature.mainActivity.di

import android.content.Context
import com.intergroupapplication.di.scope.PerActivity
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.feature.login.view.LoginActivity
import com.intergroupapplication.presentation.feature.mainActivity.view.MainActivity
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.manager.DialogProvider
import com.intergroupapplication.presentation.manager.ToastManager
import com.mobsandgeeks.saripaar.Validator
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
    fun dialogDelegate(dialogManager: DialogManager, dialogProvider: DialogProvider, toastManager: ToastManager,
                       context: Context)
            : DialogDelegate =
            DialogDelegate(dialogManager, dialogProvider, toastManager, context)


}
