package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

/**
 * Created by abakarmagomedov on 29/08/2018 at project InterGroupApplication.
 */
data class CreateGroupPostModel(@SerializedName("text") val postText: String,
                                @SerializedName("file") val imageUrl: String?)