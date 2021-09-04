package com.intergroupapplication.domain.usecase

import com.intergroupapplication.domain.gateway.UserProfileGateway
import javax.inject.Inject

class UserProfileUseCase @Inject constructor(private val userProfileGateway: UserProfileGateway) {

    fun getUserProfile() = userProfileGateway.getUserProfile()

    fun changeAvatar(avatar: String) = userProfileGateway.changeUserProfileAvatar(avatar)

}