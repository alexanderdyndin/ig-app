package com.intergroupapplication.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intergroupapplication.data.db.entity.NewsPostDb

@Dao
interface NewsPostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(newsPost: List<NewsPostDb>)

    @Query("SELECT * FROM News ORDER BY post_id DESC")
    fun getAllNewsPost(): PagingSource<Int, NewsPostDb>

    @Query("DELETE FROM News")
    fun clearAllNewsPost()
}