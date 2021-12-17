package com.intergroupapplication.domain.entity

/**
 * Created by abakarmagomedov on 06/08/2018 at project InterGroupApplication.
 */

data class UserEntity(
    val id: String,
    val firstName: String,
    val surName: String,
    val birthday: String,
    val gender: String,
    val email: String,
    val isBlocked: Boolean,
    val isActive: Boolean,
    val avatar: String?,
    val stats: StatsEntity
)
