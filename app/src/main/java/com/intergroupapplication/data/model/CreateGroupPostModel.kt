package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

/**
 * Created by abakarmagomedov on 29/08/2018 at project InterGroupApplication.
 */
data class CreateGroupPostModel(@SerializedName("text") val postText: String,
                                @SerializedName("file") val imageUrl: String?,
                                @SerializedName("image_files") val images: List<FileModel>,
                                @SerializedName("audio_files") val audios: List<AudiosModel>,
                                @SerializedName("video_files") val videos: List<FileModel>
                                )