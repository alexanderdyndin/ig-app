package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

/**
 * Created by abakarmagomedov on 28/08/2018 at project InterGroupApplication.
 */
data class CreateGroupModel(
    val name: String,
    val description: String,
    val avatar: String?,
    val subject: String,
    val rules: String,
    @SerializedName("is_closed")
    val isClosed: Boolean,
    @SerializedName("age_restriction")
    val ageRestriction: String
)
