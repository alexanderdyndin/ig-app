package com.intergroupapplication.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intergroupapplication.data.db.entity.GroupPostRemoteKeysEntity


@Dao
interface GroupPostKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertKey(remoteKey: GroupPostRemoteKeysEntity)

    @Query("SELECT * FROM group_post_remote_keys WHERE group_id = :groupId")
    fun getRemoteKey(groupId: String): GroupPostRemoteKeysEntity?

    @Query("DELETE FROM group_post_remote_keys WHERE group_id = :groupId")
    fun deleteByGroupId(groupId: String)
}