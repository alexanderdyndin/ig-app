package com.intergroupapplication.presentation.feature.login.di

import android.content.Context
import com.intergroupapplication.di.scope.PerActivity
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.feature.login.view.LoginActivity
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.manager.DialogProvider
import com.intergroupapplication.presentation.manager.ToastManager
import com.mobsandgeeks.saripaar.Validator
import dagger.Module
import dagger.Provides
import ru.terrakok.cicerone.android.support.SupportAppNavigator

@Module
class LoginViewModule {

    @PerActivity
    @Provides
    fun provideValidator(activity: LoginActivity): Validator =
            Validator(activity).apply { setValidationListener(activity) }


    @PerActivity
    @Provides
    fun provideDialogManager(activity: LoginActivity): DialogManager =
            DialogManager(activity.supportFragmentManager)


    @PerActivity
    @Provides
    fun dialogDelegate(dialogManager: DialogManager, dialogProvider: DialogProvider, toastManager: ToastManager,
                       context: Context)
            : DialogDelegate =
            DialogDelegate(dialogManager, dialogProvider, toastManager, context)


    @PerActivity
    @Provides
    fun provideSupportAppNavigator(activity: LoginActivity): SupportAppNavigator =
            SupportAppNavigator(activity, 0)
}
