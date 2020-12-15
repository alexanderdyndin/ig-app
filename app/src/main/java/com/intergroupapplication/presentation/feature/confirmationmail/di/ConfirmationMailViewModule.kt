package com.intergroupapplication.presentation.feature.confirmationmail.di

import android.content.Context
import com.intergroupapplication.di.scope.PerActivity
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.domain.entity.RegistrationEntity
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.feature.confirmationmail.view.ConfirmationMailActivity
import com.intergroupapplication.presentation.feature.confirmationmail.view.ConfirmationMailActivity.Companion.REGISTRATION_ENTITY
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.manager.DialogProvider
import com.intergroupapplication.presentation.manager.ToastManager
import dagger.Module
import dagger.Provides
import ru.terrakok.cicerone.android.support.SupportAppNavigator

@Module
class ConfirmationMailViewModule {

    @PerFragment
    @Provides
    fun provideDialogManager(activity: ConfirmationMailActivity): DialogManager =
            DialogManager(activity.requireActivity().supportFragmentManager)


    @PerFragment
    @Provides
    fun dialogDelegate(dialogManager: DialogManager, dialogProvider: DialogProvider, toastManager: ToastManager,
                       context: Context)
            : DialogDelegate = DialogDelegate(dialogManager, dialogProvider, toastManager, context)


    @PerFragment
    @Provides
    fun provideSupportAppNavigator(activity: ConfirmationMailActivity): SupportAppNavigator =
            SupportAppNavigator(activity.requireActivity(), 0)


}
