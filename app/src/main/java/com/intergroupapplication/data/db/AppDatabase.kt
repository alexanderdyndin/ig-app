package com.intergroupapplication.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.intergroupapplication.data.db.dao.PostDao
import com.intergroupapplication.data.db.entity.GroupPostModel

@Database(entities = [GroupPostModel::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
}