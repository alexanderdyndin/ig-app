package com.intergroupapplication.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.intergroupapplication.data.db.dao.GroupPostDao
import com.intergroupapplication.data.db.dao.GroupPostKeyDao
import com.intergroupapplication.data.db.dao.NewsPostDao
import com.intergroupapplication.data.db.dao.NewsPostKeyDao
import com.intergroupapplication.data.db.entity.GroupPostDb
import com.intergroupapplication.data.db.entity.GroupPostRemoteKeysDb
import com.intergroupapplication.data.db.entity.NewsPostDb
import com.intergroupapplication.data.db.entity.NewsPostRemoteKeyDb

@Database(
    entities = [
        GroupPostDb::class,
        GroupPostRemoteKeysDb::class,
        NewsPostRemoteKeyDb::class,
        NewsPostDb::class
    ],
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