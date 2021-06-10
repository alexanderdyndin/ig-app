package com.intergroupapplication.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intergroupapplication.data.db.entity.GroupPostRemoteKeysModel
import io.reactivex.Single


@Dao
interface GroupPostKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(remoteKey: GroupPostRemoteKeysModel?)

    @Query("SELECT * FROM group_post_remote_keys WHERE groupId = :groupId")
    fun getRemoteKey(groupId: String): Single<GroupPostRemoteKeysModel>

    @Query("DELETE FROM group_post_remote_keys WHERE groupId = :groupId")
    fun deleteByGroupId(groupId: String?)
}