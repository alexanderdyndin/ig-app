package com.intergroupapplication.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intergroupapplication.data.db.entity.NewsPostRemoteKeyEntity

@Dao
interface NewsPostKeyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertKey(remoteKey: NewsPostRemoteKeyEntity)

    @Query("SELECT * FROM news_post_remote_key")
    fun getRemoteKey(): NewsPostRemoteKeyEntity
}