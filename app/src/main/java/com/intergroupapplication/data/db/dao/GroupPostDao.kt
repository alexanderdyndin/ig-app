package com.intergroupapplication.data.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.intergroupapplication.data.db.entity.GroupPostDbModel

@Dao
interface GroupPostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(groupPosts: List<GroupPostDbModel>)

    @Query("SELECT * FROM group_post WHERE groupId = :groupId")
    fun getAllGroupPostsModel(groupId: String): PagingSource<Int, GroupPostDbModel>

    @Query("DELETE FROM group_post WHERE groupId = :groupId")
    fun clearAllGroupPosts(groupId: String)
}