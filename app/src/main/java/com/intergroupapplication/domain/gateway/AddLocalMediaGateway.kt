package com.intergroupapplication.domain.gateway

import com.intergroupapplication.data.model.AudioInAddFileModel
import com.intergroupapplication.data.model.GalleryModel
import com.intergroupapplication.data.model.VideoModel

interface AddLocalMediaGateway {
    fun addGalleryUri(): List<GalleryModel>
    fun addVideoUri(): List<VideoModel>
    fun addAudioUri(): List<AudioInAddFileModel>
    fun addColors(): List<Int>
}
