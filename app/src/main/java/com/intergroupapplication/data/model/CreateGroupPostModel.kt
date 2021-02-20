package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

/**
 * Created by abakarmagomedov on 29/08/2018 at project InterGroupApplication.
 */
data class CreateGroupPostModel(@SerializedName("text") val postText: String,
                                val images: List<FileModel>,
                                val audios: List<AudiosModel>,
                                val videos: List<FileModel>,
                                @SerializedName("is_pinned") val isPinned: Boolean,
                                @SerializedName("pin_time") val pinTime: String?
                                )