package com.intergroupapplication.presentation.feature.group.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import androidx.paging.rxjava2.cachedIn
import com.appodeal.ads.Appodeal
import com.appodeal.ads.NativeAd
import com.intergroupapplication.domain.usecase.PostsUseCase
import com.intergroupapplication.presentation.feature.group.adapter.GroupPostsAdapter
import com.intergroupapplication.presentation.feature.news.other.GroupPostEntityUI
import io.reactivex.Flowable
import javax.inject.Inject

class GroupViewModel @Inject constructor(private val useCase: PostsUseCase): ViewModel() {

    private val nativeAdItem: NativeAd?
        get() {
            val ads = Appodeal.getNativeAds(1)
            return if (ads.isNotEmpty()) ads[0] else null
        }

    fun fetchPosts(groupId: String): Flowable<PagingData<GroupPostEntityUI>> {
        return useCase.getGroupPosts(groupId)
                .map { pagingData ->
                    var i = -GroupPostsAdapter.AD_FIRST - 1
                    pagingData.map {
                        GroupPostEntityUI.GroupPostEntity(
                                it.id,
                                it.groupInPost,
                                it.postText,
                                it.date,
                                it.updated,
                                it.author,
                                it.unreadComments,
                                it.pin,
                                it.photo,
                                it.commentsCount,
                                it.activeCommentsCount,
                                it.isActive,
                                it.isOffered,
                                it.images,
                                it.audios,
                                it.videos
                        )
                    }
                            .insertSeparators<GroupPostEntityUI.GroupPostEntity, GroupPostEntityUI>
                            { before: GroupPostEntityUI.GroupPostEntity?, after: GroupPostEntityUI.GroupPostEntity? ->
                                i++
                                when {
                                    before == null -> null
                                    after == null -> null
                                    else -> if ( i % GroupPostsAdapter.AD_FREQ == 0 && i >= 0) {
                                        var nativeAd: NativeAd?
                                        if (nativeAdItem.also { nativeAd = it } != null) {
                                            GroupPostEntityUI.AdEntity(i, nativeAd)
                                        } else null
                                    } else null
                                }
                            }
                }
                .cachedIn(viewModelScope)
    }
}