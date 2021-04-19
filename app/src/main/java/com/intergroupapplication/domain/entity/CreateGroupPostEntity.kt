package com.intergroupapplication.domain.entity

/**
 * Created by abakarmagomedov on 30/08/2018 at project InterGroupApplication.
 */
data class CreateGroupPostEntity(val postText: String,
                                 val images: List<FileRequestEntity>,
                                 val audios: List<AudioRequestEntity>,
                                 val videos: List<FileRequestEntity>,
                                 val isPinned: Boolean,
                                 val pinTime: String?
                                 )