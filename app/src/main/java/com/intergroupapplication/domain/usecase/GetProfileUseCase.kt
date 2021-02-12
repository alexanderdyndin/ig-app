package com.intergroupapplication.domain.usecase

import com.intergroupapplication.domain.gateway.UserProfileGateway
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(private val userProfileGateway: UserProfileGateway) {

    fun getUserProfile() = userProfileGateway.getUserProfile()

    fun getAdParameters() = userProfileGateway.getAdParameters()
}