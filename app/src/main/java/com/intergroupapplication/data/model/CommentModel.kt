package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

/**
 * Created by abakarmagomedov on 03/09/2018 at project InterGroupApplication.
 */
data class CommentModel(val id: String,
                        @SerializedName("user") val commentOwner: CommentUserModel?,
                        val reacts: ReactsModel,
                        val images: List<ImageVideoModel>,
                        val audios: List<AudioModel>,
                        val videos: List<ImageVideoModel>,
                        val text: String,
                        val date: String,
                        val isActive: Boolean,
                        @SerializedName("unique_index") val idc: Int,
                        val post: Int,
                        @SerializedName("answer_to") val answerTo: CommentModel?)