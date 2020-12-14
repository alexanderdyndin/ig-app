package com.intergroupapplication.presentation.feature.agreements.di

import android.content.Context
import com.intergroupapplication.di.scope.PerActivity
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.feature.agreements.view.AgreementsActivity
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.manager.DialogProvider
import com.intergroupapplication.presentation.manager.ToastManager
import dagger.Module
import dagger.Provides
import ru.terrakok.cicerone.android.support.SupportAppNavigator

@Module
class AgreementsViewModule {

    @PerActivity
    @Provides
    fun provideDialogManager(activity: AgreementsActivity): DialogManager =
            DialogManager(activity.requireActivity().supportFragmentManager)


    @PerActivity
    @Provides
    fun dialogDelegate(dialogManager: DialogManager, dialogProvider: DialogProvider, toastManager: ToastManager,
                       context: Context)
            : DialogDelegate = DialogDelegate(dialogManager, dialogProvider, toastManager, context)

//    @PerActivity
//    @Provides
//    fun provideNavigator(activity: AgreementsActivity) =
//            SupportAppNavigator(activity, 0)

}