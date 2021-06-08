package com.intergroupapplication.data.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class GroupPostWithVideoModel(
        @Embedded val groupPost: GroupPostDbModel,
        @Relation(
                parentColumn = "id",
                entityColumn = "id"
        )
        val images: List<VideoModel>
)




