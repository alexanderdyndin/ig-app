package com.intergroupapplication.presentation.feature.recoveryPassword.di

import android.content.Context
import com.intergroupapplication.di.scope.PerActivity
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.feature.recoveryPassword.view.RecoveryPasswordActivity
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.manager.DialogProvider
import com.intergroupapplication.presentation.manager.ToastManager
import com.mobsandgeeks.saripaar.Validator
import dagger.Module
import dagger.Provides
import ru.terrakok.cicerone.android.support.SupportAppNavigator

@Module
class RecoveryPasswordModule {

    @PerActivity
    @Provides
    fun provideDialogManager(activity: RecoveryPasswordActivity): DialogManager =
            DialogManager(activity.supportFragmentManager)


    @PerActivity
    @Provides
    fun dialogDelegate(dialogManager: DialogManager, dialogProvider: DialogProvider, toastManager: ToastManager,
                       context: Context)
            : DialogDelegate =
            DialogDelegate(dialogManager, dialogProvider, toastManager, context)


    @PerActivity
    @Provides
    fun provideSupportAppNavigator(activity: RecoveryPasswordActivity): SupportAppNavigator =
            SupportAppNavigator(activity, 0)

    @PerActivity
    @Provides
    fun provideValidator(activity: RecoveryPasswordActivity): Validator =
            Validator(activity).apply { setValidationListener(activity) }

}