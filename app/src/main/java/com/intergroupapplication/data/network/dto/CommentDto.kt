package com.intergroupapplication.data.network.dto

import com.google.gson.annotations.SerializedName
import com.intergroupapplication.data.model.AudioModel
import com.intergroupapplication.data.model.CommentUserModel
import com.intergroupapplication.data.model.ImageVideoModel

/**
 * Created by abakarmagomedov on 03/09/2018 at project InterGroupApplication.
 */
data class CommentDto(
    val id: String,
    @SerializedName("user")
    val commentOwner: CommentUserModel?,
    val reacts: ReactsDto,
    val images: List<ImageVideoModel>,
    val audios: List<AudioModel>,
    val videos: List<ImageVideoModel>,
    val text: String,
    val date: String,
    val isActive: Boolean,
    @SerializedName("unique_index")
    val idc: Int,
    val post: Int,
    @SerializedName("answer_to")
    val answerTo: CommentDto?
)
