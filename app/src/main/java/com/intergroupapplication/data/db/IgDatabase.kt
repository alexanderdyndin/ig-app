package com.intergroupapplication.data.db

import androidx.room.*
import com.intergroupapplication.data.db.dao.GroupPostDao
import com.intergroupapplication.data.db.dao.GroupPostKeyDao
import com.intergroupapplication.data.db.entity.GroupPostRemoteKeysModel
import com.intergroupapplication.data.model.GroupPostModel

@Database(entities = [GroupPostModel::class, GroupPostRemoteKeysModel::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class IgDatabase : RoomDatabase() {

    abstract fun groupPostKeyDao(): GroupPostKeyDao
    abstract fun groupPostDao(): GroupPostDao
}