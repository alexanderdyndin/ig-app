package com.intergroupapplication.presentation.feature.confirmationmail.di

import android.content.Context
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.feature.confirmationmail.view.ConfirmationMailFragment
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.manager.DialogProvider
import com.intergroupapplication.presentation.manager.ToastManager
import com.workable.errorhandler.ErrorHandler
import dagger.Module
import dagger.Provides
import javax.inject.Named


@Module
class ConfirmationMailViewModule {

    @PerFragment
    @Provides
    fun provideDialogManager(fragment: ConfirmationMailFragment): DialogManager =
            DialogManager(fragment.requireActivity().supportFragmentManager)


    @PerFragment
    @Provides
    fun dialogDelegate(dialogManager: DialogManager, dialogProvider: DialogProvider, toastManager: ToastManager,
                       context: Context)
            : DialogDelegate = DialogDelegate(dialogManager, dialogProvider, toastManager, context)



}
