package com.intergroupapplication.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.intergroupapplication.data.db.dao.GroupPostDao
import com.intergroupapplication.data.db.dao.GroupPostKeyDao
import com.intergroupapplication.data.db.entity.GroupPostDbModel
import com.intergroupapplication.data.db.entity.GroupPostRemoteKeysModel

@Database(entities = [GroupPostDbModel::class, GroupPostRemoteKeysModel::class], version = 1, exportSchema = false)
abstract class IgDatabase : RoomDatabase() {
    abstract fun groupPost(): GroupPostDao
    abstract fun groupPostKey(): GroupPostKeyDao

    companion object {

        @Volatile
        private var INSTANCE: IgDatabase? = null

        fun getInstance(context: Context): IgDatabase =
                INSTANCE ?: synchronized(this) {
                    INSTANCE
                            ?: buildDatabase(context).also { INSTANCE = it }
                }

        private fun buildDatabase(context: Context) =
                Room.databaseBuilder(context.applicationContext, IgDatabase::class.java, "ig-app.db")
                        .build()
    }
}