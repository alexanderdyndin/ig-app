package com.intergroupapplication.domain.entity


/**
 * Created by abakarmagomedov on 28/08/2018 at project InterGroupApplication.
 */
data class CreateGroupEntity(
    val name: String,
    val description: String,
    val avatar: String?,
    val subject: String,
    val rules: String,
    val isClosed: Boolean,
    val ageRestriction: String
)
