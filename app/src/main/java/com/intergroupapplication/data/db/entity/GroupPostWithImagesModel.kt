package com.intergroupapplication.data.db.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.intergroupapplication.data.model.ImageVideoModel

data class GroupPostWithImagesModel(
        @Embedded val groupPost: GroupPostDbModel,
        @Relation(
                parentColumn = "id",
                entityColumn = "id"
        )
        val video: List<ImageVideoModel>
)




