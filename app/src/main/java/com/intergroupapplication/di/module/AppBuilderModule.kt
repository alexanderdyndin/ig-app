package com.intergroupapplication.di.module

import com.intergroupapplication.device.service.InterGroupPushService
import com.intergroupapplication.di.scope.PerActivity
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.di.scope.PerService
import com.intergroupapplication.presentation.feature.agreements.di.AgreementsViewModule
import com.intergroupapplication.presentation.feature.agreements.view.AgreementsFragment
import com.intergroupapplication.presentation.feature.audiolist.di.AudioListViewModule
import com.intergroupapplication.presentation.feature.audiolist.view.AudioListFragment
import com.intergroupapplication.presentation.feature.commentsdetails.di.BottomSheetViewModule
import com.intergroupapplication.presentation.feature.group.di.GroupViewModule
import com.intergroupapplication.presentation.feature.group.view.GroupFragment
import com.intergroupapplication.presentation.feature.commentsdetails.di.CommentsDetailsViewModule
import com.intergroupapplication.presentation.feature.commentsdetails.view.BottomSheetFragment
import com.intergroupapplication.presentation.feature.commentsdetails.view.CommentsDetailsFragment
import com.intergroupapplication.presentation.feature.confirmationmail.di.ConfirmationMailViewModule
import com.intergroupapplication.presentation.feature.confirmationmail.di.ConfirmationUserProfileGatewayModule
import com.intergroupapplication.presentation.feature.confirmationmail.view.ConfirmationMailFragment
import com.intergroupapplication.presentation.feature.creategroup.di.CreateGroupViewModule
import com.intergroupapplication.presentation.feature.creategroup.view.CreateGroupFragment
import com.intergroupapplication.presentation.feature.createpost.di.CreatePostViewModule
import com.intergroupapplication.presentation.feature.createpost.view.CreatePostFragment
import com.intergroupapplication.presentation.feature.createuserprofile.di.CreateUserProfileViewModule
import com.intergroupapplication.presentation.feature.createuserprofile.view.CreateUserProfileFragment
import com.intergroupapplication.presentation.feature.grouplist.di.GroupListViewModule
import com.intergroupapplication.presentation.feature.grouplist.view.GroupListFragment
import com.intergroupapplication.presentation.feature.image.di.ImageViewModule
import com.intergroupapplication.presentation.feature.image.view.ImageFragment
import com.intergroupapplication.presentation.feature.login.di.LoginViewModule
import com.intergroupapplication.presentation.feature.login.view.LoginFragment
import com.intergroupapplication.presentation.feature.mainActivity.di.MainActivityViewModule
import com.intergroupapplication.presentation.feature.mainActivity.view.MainActivity
import com.intergroupapplication.presentation.feature.news.di.NewsViewModule
import com.intergroupapplication.presentation.feature.news.view.NewsFragment
import com.intergroupapplication.presentation.feature.recoveryPassword.di.RecoveryPasswordModule
import com.intergroupapplication.presentation.feature.recoveryPassword.view.RecoveryPasswordFragment
import com.intergroupapplication.presentation.feature.registration.di.RegistrationViewModule
import com.intergroupapplication.presentation.feature.registration.view.RegistrationFragment
import com.intergroupapplication.presentation.feature.splash.SplashFragment
import com.intergroupapplication.presentation.feature.splash.di.SplashViewModule
import com.intergroupapplication.presentation.feature.userlist.di.UserListViewModule
import com.intergroupapplication.presentation.feature.userlist.view.UserListFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface AppBuilderModule {

    @PerService
    @ContributesAndroidInjector()
    fun provideInterGroupPushService(): InterGroupPushService

    @PerFragment
    @ContributesAndroidInjector(modules = [(LoginViewModule::class)])
    fun provideLoginActivityFactory(): LoginFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [(RegistrationViewModule::class)])
    fun provideRegistrationActivityFactory(): RegistrationFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [(CreateUserProfileViewModule::class)])
    fun provideCreateProfileActivityFactory(): CreateUserProfileFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [ConfirmationMailViewModule::class,
        ConfirmationUserProfileGatewayModule::class])
    fun provideConfirmationMailActivityFactory(): ConfirmationMailFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [SplashViewModule::class])
    fun provideSplashActivityFactory(): SplashFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [CreateGroupViewModule::class])
    fun provideCreateGroupActivityFactory(): CreateGroupFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [GroupViewModule::class])
    fun provideAdminGroupActivityFactory(): GroupFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [CommentsDetailsViewModule::class])
    fun provideCommentsDetailsActivityFactory(): CommentsDetailsFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [BottomSheetViewModule::class])
    fun provideBottomSheetFragment():BottomSheetFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [CreatePostViewModule::class])
    fun provideCreatePostFragmentFactory(): CreatePostFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [AgreementsViewModule::class])
    fun provideAgreementsFragmentFactory(): AgreementsFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [RecoveryPasswordModule::class])
    fun provideRecoveryPasswordFactory(): RecoveryPasswordFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [GroupListViewModule::class])
    fun provideGroupListFragmentFactory(): GroupListFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [NewsViewModule::class])
    fun provideNewsFragmentFactory(): NewsFragment

    @PerActivity
    @ContributesAndroidInjector(modules = [MainActivityViewModule::class])
    fun provideMainActivityFactory(): MainActivity

    @PerFragment
    @ContributesAndroidInjector(modules = [UserListViewModule::class])
    fun provideUserListFragmentFactory(): UserListFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [ImageViewModule::class])
    fun provideImageFragmentFactory(): ImageFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [AudioListViewModule::class])
    fun provideAudioFragmentFactory(): AudioListFragment

}
