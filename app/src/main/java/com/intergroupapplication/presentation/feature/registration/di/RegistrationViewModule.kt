package com.intergroupapplication.presentation.feature.registration.di

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.intergroupapplication.di.scope.PerActivity
import com.intergroupapplication.presentation.Screens
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.feature.confirmationmail.view.ConfirmationMailActivity
import com.intergroupapplication.presentation.feature.createuserprofile.view.CreateUserProfileActivity
import com.intergroupapplication.presentation.feature.login.view.LoginActivity
import com.intergroupapplication.presentation.feature.registration.view.RegistrationActivity
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.manager.DialogProvider
import com.intergroupapplication.presentation.manager.ToastManager
import com.mobsandgeeks.saripaar.Validator
import dagger.Module
import dagger.Provides
import ru.terrakok.cicerone.android.support.SupportAppNavigator

@Module
class RegistrationViewModule {

    @PerActivity
    @Provides
    fun provideDialogManager(activity: RegistrationActivity): DialogManager =
            DialogManager(activity.supportFragmentManager)


    @PerActivity
    @Provides
    fun dialogDelegate(dialogManager: DialogManager, dialogProvider: DialogProvider, toastManager: ToastManager,
                       context: Context)
            : DialogDelegate =
            DialogDelegate(dialogManager, dialogProvider, toastManager, context)


    @PerActivity
    @Provides
    fun provideValidator(activity: RegistrationActivity): Validator =
            Validator(activity).apply { setValidationListener(activity) }

    @PerActivity
    @Provides
    fun provideSupportAppNavigator(activity: RegistrationActivity): SupportAppNavigator =
            SupportAppNavigator(activity, 0)
}