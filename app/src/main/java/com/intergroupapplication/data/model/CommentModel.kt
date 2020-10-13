package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

/**
 * Created by abakarmagomedov on 03/09/2018 at project InterGroupApplication.
 */
data class CommentModel(val id: String,
                        val text: String,
                        val date: String,
                        @SerializedName("user") val commentOwner: CommentUserModel?,
                        @SerializedName("answer_to") val answerTo: String?)