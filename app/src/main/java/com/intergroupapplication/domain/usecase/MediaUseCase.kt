package com.intergroupapplication.domain.usecase

import com.intergroupapplication.domain.gateway.MediaGateway
import javax.inject.Inject

class MediaUseCase @Inject constructor(private val mediaGateway: MediaGateway) {

    fun getAudio() = mediaGateway.getAudioList()

    fun getVideo() = mediaGateway.getVideoList()

    fun getImages() = mediaGateway.getImageList()
}
