package com.intergroupapplication.data.db

import androidx.room.*
import com.intergroupapplication.data.db.dao.GroupPostDao
import com.intergroupapplication.data.db.dao.GroupPostKeyDao
import com.intergroupapplication.data.db.dao.NewsPostDao
import com.intergroupapplication.data.db.dao.NewsPostKeyDao
import com.intergroupapplication.data.db.entity.GroupPostRemoteKeysEntity
import com.intergroupapplication.data.db.entity.NewsPostDb
import com.intergroupapplication.data.db.entity.NewsPostRemoteKeyEntity
import com.intergroupapplication.data.model.GroupPostModel

@Database(
    entities = [GroupPostModel::class,
        GroupPostRemoteKeysEntity::class,
        NewsPostRemoteKeyEntity::class,
        NewsPostDb::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class IgDatabase : RoomDatabase() {

    abstract fun groupPostKeyDao(): GroupPostKeyDao
    abstract fun groupPostDao(): GroupPostDao
    abstract fun newsPostKeyDao(): NewsPostKeyDao
    abstract fun newsPostDao(): NewsPostDao
}