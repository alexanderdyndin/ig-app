package com.intergroupapplication.presentation.feature.login.di

import android.content.Context
import com.intergroupapplication.di.qualifier.LoginHandler
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.feature.login.view.LoginFragment
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.provider.DialogProvider
import com.mobsandgeeks.saripaar.Validator
import com.workable.errorhandler.ErrorHandler
import dagger.Module
import dagger.Provides


@Module
class LoginViewModule {

    @PerFragment
    @Provides
    fun provideValidator(fragment: LoginFragment): Validator =
        Validator(fragment).apply { setValidationListener(fragment) }


    @PerFragment
    @Provides
    fun provideDialogManager(fragment: LoginFragment): DialogManager =
        DialogManager(fragment.requireActivity().supportFragmentManager)


    @PerFragment
    @Provides
    fun dialogDelegate(
        dialogManager: DialogManager, dialogProvider: DialogProvider,
        context: Context
    )
            : DialogDelegate =
        DialogDelegate(dialogManager, dialogProvider, context)

    @PerFragment
    @Provides
    @LoginHandler
    fun errorHandler(): ErrorHandler = ErrorHandler.createIsolated()
}
