package com.intergroupapplication.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intergroupapplication.data.db.entity.GroupPostRemoteKeysDb


@Dao
interface GroupPostKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertKey(remoteKey: GroupPostRemoteKeysDb)

    @Query("SELECT * FROM group_post_remote_keys WHERE group_id = :groupId")
    fun getRemoteKey(groupId: String): GroupPostRemoteKeysDb?

    @Query("DELETE FROM group_post_remote_keys WHERE group_id = :groupId")
    fun deleteByGroupId(groupId: String)
}