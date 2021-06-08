package com.intergroupapplication.data.db.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.intergroupapplication.data.model.AudioModel

data class GroupPostWithAudioModel(
        @Embedded val groupPost: GroupPostDbModel,
        @Relation(
                parentColumn = "id",
                entityColumn = "id"
        )
        val audio: List<AudioModel>
)




