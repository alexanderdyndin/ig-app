package com.intergroupapplication.presentation.feature.registration.di

import android.content.Context
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.feature.registration.view.RegistrationFragment
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.manager.ToastManager
import com.intergroupapplication.presentation.provider.DialogProvider
import com.mobsandgeeks.saripaar.Validator
import com.workable.errorhandler.ErrorHandler
import dagger.Module
import dagger.Provides
import javax.inject.Named


@Module
class RegistrationViewModule {

    @PerFragment
    @Provides
    fun provideDialogManager(fragment: RegistrationFragment): DialogManager =
            DialogManager(fragment.requireActivity().supportFragmentManager)


    @PerFragment
    @Provides
    fun dialogDelegate(dialogManager: DialogManager, dialogProvider: DialogProvider, toastManager: ToastManager,
                       context: Context)
            : DialogDelegate =
            DialogDelegate(dialogManager, dialogProvider, toastManager, context)


    @PerFragment
    @Provides
    fun provideValidator(fragment: RegistrationFragment): Validator =
            Validator(fragment.requireActivity()).apply { setValidationListener(fragment) }

    @PerFragment
    @Provides
    @Named("RegistrationHandler")
    fun errorHandler(): ErrorHandler = ErrorHandler.create()

}
