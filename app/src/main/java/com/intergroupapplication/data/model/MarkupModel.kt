package com.intergroupapplication.data.model

import com.intergroupapplication.domain.entity.AudioEntity
import com.intergroupapplication.domain.entity.FileEntity

data class MarkupModel(val text: String,
                       val images: List<FileEntity>,
                       val audios: List<AudioEntity>,
                       val videos: List<FileEntity>,
                       var imagesExpanded: Boolean = false,
                       var audiosExpanded: Boolean = false,
                       var videosExpanded: Boolean = false
                       )
