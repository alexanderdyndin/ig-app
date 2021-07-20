package com.intergroupapplication.presentation.feature.agreements.di

import android.content.Context
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.feature.agreements.view.AgreementsFragment
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.provider.DialogProvider
import com.intergroupapplication.presentation.manager.ToastManager
import dagger.Module
import dagger.Provides


@Module
class AgreementsViewModule {

    @PerFragment
    @Provides
    fun provideDialogManager(activity: AgreementsFragment): DialogManager =
            DialogManager(activity.requireActivity().supportFragmentManager)


    @PerFragment
    @Provides
    fun dialogDelegate(dialogManager: DialogManager, dialogProvider: DialogProvider, toastManager: ToastManager,
                       context: Context)
            : DialogDelegate = DialogDelegate(dialogManager, dialogProvider, toastManager, context)

}