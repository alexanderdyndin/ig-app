package com.intergroupapplication.data.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.intergroupapplication.data.model.GroupPostModel

@Dao
interface GroupPostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(groupPosts: List<GroupPostModel>)

    @Query("SELECT * FROM group_post WHERE groupId = :groupId ORDER BY uid")
    fun getAllGroupPostsModel(groupId: String): PagingSource<Int, GroupPostModel>

    @Query("DELETE FROM group_post WHERE groupId = :groupId")
    fun clearAllGroupPosts(groupId: String)
}