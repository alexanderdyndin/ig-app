package com.intergroupapplication.data.model

import androidx.room.ColumnInfo

/**
 * Created by abakarmagomedov on 06/09/2018 at project InterGroupApplication.
 */
data class GroupInPostModel(
        @ColumnInfo(name = "group_in_post_id") val id: String,
        @ColumnInfo(name = "group_in_post_name") val name: String,
        @ColumnInfo(name = "group_in_post_avatar") val avatar: String?)