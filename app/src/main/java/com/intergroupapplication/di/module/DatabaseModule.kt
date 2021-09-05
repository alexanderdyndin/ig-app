package com.intergroupapplication.di.module

import androidx.room.Room
import com.intergroupapplication.App
import com.intergroupapplication.data.db.IgDatabase
import com.intergroupapplication.data.db.dao.GroupPostDao
import com.intergroupapplication.data.db.dao.GroupPostKeyDao
import com.intergroupapplication.di.scope.PerApplication
import dagger.Module
import dagger.Provides

@Module
class DatabaseModule {

    @PerApplication
    @Provides
    fun providesRoomDatabase(app: App): IgDatabase {
        return Room.databaseBuilder(
            app.applicationContext,
            IgDatabase::class.java,
            "ig-app.db"
        ).build()
    }

    @PerApplication
    @Provides
    fun provideGroupPostDao(db: IgDatabase): GroupPostDao = db.groupPostDao()

    @PerApplication
    @Provides
    fun provideGroupPostKeyDao(db: IgDatabase): GroupPostKeyDao = db.groupPostKeyDao()

}
