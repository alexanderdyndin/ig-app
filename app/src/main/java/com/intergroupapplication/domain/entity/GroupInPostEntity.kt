package com.intergroupapplication.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by abakarmagomedov on 06/09/2018 at project InterGroupApplication.
 */
@Parcelize
data class GroupInPostEntity(
    val id: String,
    val name: String,
    val avatar: String?
) : Parcelable
