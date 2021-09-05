package com.intergroupapplication.presentation.feature.confirmationmail.di

import com.intergroupapplication.data.service.ConfirmationMailService
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.domain.gateway.ConfirmationMailGateway
import dagger.Binds
import dagger.Module

/**
 * Created by abakarmagomedov on 08/08/2018 at project InterGroupApplication.
 */
@Module
interface ConfirmationUserProfileGatewayModule {

    @PerFragment
    @Binds
    fun provideConfirmationMailGateway(confirmationMailService: ConfirmationMailService): ConfirmationMailGateway

}
