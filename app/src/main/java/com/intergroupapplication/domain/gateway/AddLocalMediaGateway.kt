package com.intergroupapplication.domain.gateway

import com.intergroupapplication.data.model.AudioInAddFileModel
import com.intergroupapplication.data.model.GalleryModel
import com.intergroupapplication.data.model.VideoModel

interface AddLocalMediaGateway {
    fun addGalleryUri():MutableList<GalleryModel>
    fun addVideoUri():MutableList<VideoModel>
    fun addAudioUri():MutableList<AudioInAddFileModel>
}