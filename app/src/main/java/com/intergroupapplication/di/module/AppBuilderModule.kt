package com.intergroupapplication.di.module

import com.intergroupapplication.device.service.InterGroupPushService
import com.intergroupapplication.di.scope.PerActivity
import com.intergroupapplication.di.scope.PerService
import com.intergroupapplication.presentation.feature.agreements.di.AgreementsViewModule
import com.intergroupapplication.presentation.feature.agreements.view.AgreementsActivity
import com.intergroupapplication.presentation.feature.group.di.GroupViewModule
import com.intergroupapplication.presentation.feature.group.view.GroupActivity
import com.intergroupapplication.presentation.feature.commentsdetails.di.CommentsDetailsViewModule
import com.intergroupapplication.presentation.feature.commentsdetails.view.CommentsDetailsActivity
import com.intergroupapplication.presentation.feature.confirmationmail.di.ConfirmationMailViewModule
import com.intergroupapplication.presentation.feature.confirmationmail.di.ConfirmationUserProfileGatewayModule
import com.intergroupapplication.presentation.feature.confirmationmail.view.ConfirmationMailActivity
import com.intergroupapplication.presentation.feature.creategroup.di.CreateGroupViewModule
import com.intergroupapplication.presentation.feature.creategroup.view.CreateGroupActivity
import com.intergroupapplication.presentation.feature.createpost.di.CreatePostViewModule
import com.intergroupapplication.presentation.feature.createpost.view.CreatePostActivity
import com.intergroupapplication.presentation.feature.createuserprofile.di.CreateUserProfileViewModule
import com.intergroupapplication.presentation.feature.createuserprofile.view.CreateUserProfileActivity
import com.intergroupapplication.presentation.feature.login.di.LoginViewModule
import com.intergroupapplication.presentation.feature.login.view.LoginActivity
import com.intergroupapplication.presentation.feature.navigation.di.NavigationBuilderModule
import com.intergroupapplication.presentation.feature.navigation.di.NavigationViewModule
import com.intergroupapplication.presentation.feature.navigation.view.NavigationActivity
import com.intergroupapplication.presentation.feature.recoveryPassword.di.RecoveryPasswordModule
import com.intergroupapplication.presentation.feature.recoveryPassword.view.RecoveryPasswordActivity
import com.intergroupapplication.presentation.feature.registration.di.RegistrationViewModule
import com.intergroupapplication.presentation.feature.registration.view.RegistrationActivity
import com.intergroupapplication.presentation.feature.splash.SplashActivity
import com.intergroupapplication.presentation.feature.splash.di.SplashViewModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface AppBuilderModule {

    @PerService
    @ContributesAndroidInjector()
    fun provideInterGroupPushService(): InterGroupPushService

    @PerActivity
    @ContributesAndroidInjector(modules = [(LoginViewModule::class)])
    fun provideLoginActivityFactory(): LoginActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [(RegistrationViewModule::class)])
    fun provideRegistrationActivityFactory(): RegistrationActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [(CreateUserProfileViewModule::class)])
    fun provideCreateProfileActivityFactory(): CreateUserProfileActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [(NavigationViewModule::class), (NavigationBuilderModule::class)])
    fun provideNavigationActivityFactory(): NavigationActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [ConfirmationMailViewModule::class,
        ConfirmationUserProfileGatewayModule::class])
    fun provideConfirmationMailActivityFactory(): ConfirmationMailActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [SplashViewModule::class])
    fun provideSplashActivityFactory(): SplashActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [CreateGroupViewModule::class])
    fun provideCreateGroupActivityFactory(): CreateGroupActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [GroupViewModule::class])
    fun provideAdminGroupActivityFactory(): GroupActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [CommentsDetailsViewModule::class])
    fun provideCommentsDetailsActivityFactory(): CommentsDetailsActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [CreatePostViewModule::class])
    fun provideCreatePostActivityFactory(): CreatePostActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [AgreementsViewModule::class])
    fun provideAgreementsActivityFactory(): AgreementsActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [RecoveryPasswordModule::class])
    fun provideRecoveryPasswordFactory(): RecoveryPasswordActivity


}
