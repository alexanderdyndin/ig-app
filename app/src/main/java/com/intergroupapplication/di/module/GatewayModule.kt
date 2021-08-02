package com.intergroupapplication.di.module

import com.intergroupapplication.data.repository.*
import com.intergroupapplication.data.service.AwsUploadingService
import com.intergroupapplication.data.service.LoginService
import com.intergroupapplication.data.service.RegistrationService
import com.intergroupapplication.domain.gateway.*
import dagger.Binds
import dagger.Module

/**
 * Created by abakarmagomedov on 03/08/2018 at project InterGroupApplication.
 */

@Module
interface GatewayModule {

    @Binds
    fun provideImeiGateway(imeiRepository: ImeiRepository): ImeiGateway

    @Binds
    fun provideUserProfileGateway(userProfileRepository: UserProfileRepository): UserProfileGateway

    @Binds
    fun provideGroupGateway(groupRepository: GroupRepository): GroupGateway

    @Binds
    fun provideGroupPostGateway(groupPostRepository: GroupPostsRepository): GroupPostGateway

    @Binds
    fun provideLoginGateway(loginService: LoginService): LoginGateway

    @Binds
    fun provideRegistrationGateway(registrationService: RegistrationService): RegistrationGateway

    @Binds
    fun provideCommentsGateway(commentRepository: CommentRepository): CommentGateway

    @Binds
    fun provideAwsUploadingGateway(aswUploadingService: AwsUploadingService): AwsUploadingGateway

    @Binds
    fun provideTokenRepository(fbTokenRepository: FbTokenRepository): FbTokenGetaway

    @Binds
    fun providePermissionRepository(permissionAutorizeRepository: PermissionAutorizeRepository): PermissionAutorizeGetaway

    @Binds
    fun provideResendCodeGetawayRepository(resendCodeRepository: ResendCodeRepository): ResendCodeGateway

    @Binds
    fun provideResetPasswordRepository(resendCodeRepository: ResetPasswordRepository): ResetPasswordGetaway

    @Binds
    fun provideComplaintsRepository(complaintsRepository: ComplaintsRepository): ComplaintsGateway

    @Binds
    fun provideAppStatusRepository(appStatusRepository: AppStatusRepository): AppStatusGateway

    @Binds
    fun provideMediaRepository(mediaRepository: MediaRepository): MediaGateway

    @Binds
    fun provideAddLocalMediaRepository(addLocalMediaRepository: AddLocalMediaRepository):AddLocalMediaGateway
    @Binds
    fun provideAvatarRepository(avatarRepository: AvatarRepository): AvatarGateway
}
