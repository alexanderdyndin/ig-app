package com.intergroupapplication.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intergroupapplication.data.db.entity.NewsPostDb
import com.intergroupapplication.data.model.NewsModel

@Dao
interface NewsPostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(newsPost: List<NewsPostDb>)

    @Query("SELECT * FROM News")
    fun getAllNewsPost(): PagingSource<Int, NewsPostDb>

    @Query("DELETE FROM News")
    fun clearAllNewsPost()
}