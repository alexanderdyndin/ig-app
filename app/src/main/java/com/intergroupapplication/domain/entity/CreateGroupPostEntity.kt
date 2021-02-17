package com.intergroupapplication.domain.entity

import com.google.gson.annotations.SerializedName

/**
 * Created by abakarmagomedov on 30/08/2018 at project InterGroupApplication.
 */
data class CreateGroupPostEntity(val postText: String,
                                 val images: List<FilesEntity>,
                                 val audios: List<AudiosEntity>,
                                 val videos: List<FilesEntity>,
                                 val isPinned: Boolean,
                                 val pinTime: String?
                                 )