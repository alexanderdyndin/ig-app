package com.intergroupapplication.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intergroupapplication.data.db.entity.GroupPostDb

@Dao
interface GroupPostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(groupPosts: List<GroupPostDb>)

    @Query("SELECT * FROM group_post WHERE group_id = :groupId ORDER BY idp DESC")
    fun getAllGroupPostsModel(groupId: String): PagingSource<Int, GroupPostDb>

    @Query("DELETE FROM group_post WHERE group_id = :groupId")
    fun clearAllGroupPosts(groupId: String)
}