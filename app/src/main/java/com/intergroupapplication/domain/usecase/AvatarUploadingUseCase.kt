package com.intergroupapplication.domain.usecase

import com.intergroupapplication.domain.gateway.AvatarGateway
import javax.inject.Inject

class AvatarUploadingUseCase @Inject constructor(
    private val avatarGateway: AvatarGateway
) {

    fun upload(
        file: String,
        groupId: String? = null
    ) = avatarGateway.uploadToAws(file, groupId)
}
