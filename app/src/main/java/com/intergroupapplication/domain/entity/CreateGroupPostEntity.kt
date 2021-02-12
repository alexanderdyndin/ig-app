package com.intergroupapplication.domain.entity

import com.google.gson.annotations.SerializedName

/**
 * Created by abakarmagomedov on 30/08/2018 at project InterGroupApplication.
 */
data class CreateGroupPostEntity(val postText: String,
                                 val imageUrl: String?,
                                 val images: List<String>,
                                 val audios: List<String>,
                                 val videos: List<String>
                                 )