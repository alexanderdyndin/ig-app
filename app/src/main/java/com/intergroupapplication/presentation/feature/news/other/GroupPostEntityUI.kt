package com.intergroupapplication.presentation.feature.news.other

import com.appodeal.ads.NativeAd
import com.intergroupapplication.domain.entity.AudioEntity
import com.intergroupapplication.domain.entity.AuthorEntity
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.domain.entity.GroupInPostEntity

/**
 * Created by abakarmagomedov on 27/08/2018 at project InterGroupApplication.
 */

sealed class GroupPostEntityUI {
        data class GroupPostEntity(
                val id: String,
                val groupInPost: GroupInPostEntity,
                val postText: String,
                val date: String,
                val updated: String?,
                val author: AuthorEntity,
                val unreadComments: Int,
                val pin: String?,
                val photo: String?,
                var commentsCount: String,
                val activeCommentsCount: String,
                val isActive: Boolean,
                val isOffered: Boolean,
                val images: List<FileEntity>,
                val audios: List<AudioEntity>,
                val videos: List<FileEntity>
        ) : GroupPostEntityUI()

        data class AdEntity(val position: Int, val nativeAd: NativeAd?) : GroupPostEntityUI()
}