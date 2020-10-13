package com.intergroupapplication.domain.entity

import com.google.gson.annotations.SerializedName

/**
 * Created by abakarmagomedov on 03/09/2018 at project InterGroupApplication.
 */
data class CommentUserEntity(
        val user: Int,
        val firstName: String,
        val secondName: String,
        val birthday: String,
        val gender: String,
        val avatar: String?
)