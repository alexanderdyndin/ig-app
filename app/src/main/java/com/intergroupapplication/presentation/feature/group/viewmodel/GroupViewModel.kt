package com.intergroupapplication.presentation.feature.group.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import androidx.paging.rxjava2.cachedIn
import com.appodeal.ads.Appodeal
import com.appodeal.ads.NativeAd
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.usecase.PostsUseCase
import com.intergroupapplication.presentation.feature.group.adapter.GroupPostsAdapter
import io.reactivex.Flowable
import javax.inject.Inject

class GroupViewModel @Inject constructor(private val useCase: PostsUseCase): ViewModel() {

    private val nativeAdItem: NativeAd?
        get() {
            val ads = Appodeal.getNativeAds(1)
            return if (ads.isNotEmpty()) ads[0] else null
        }

    fun fetchPosts(groupId: String): Flowable<PagingData<GroupPostEntity>> {
        return useCase.getGroupPosts(groupId)
                .map { pagingData ->
                    var i = -GroupPostsAdapter.AD_FIRST - 1
                    pagingData.map {
                        it as GroupPostEntity.PostEntity
                    }
                            .insertSeparators<GroupPostEntity.PostEntity, GroupPostEntity>
                            { before: GroupPostEntity.PostEntity?, after: GroupPostEntity.PostEntity? ->
                                i++
                                when {
                                    before == null -> null
                                    after == null -> null
                                    else -> if ( i % GroupPostsAdapter.AD_FREQ == 0 && i >= 0) {
                                        var nativeAd: NativeAd?
                                        if (nativeAdItem.also { nativeAd = it } != null) {
                                            GroupPostEntity.AdEntity(i, nativeAd)
                                        } else null
                                    } else null
                                }
                            }
                }
                .cachedIn(viewModelScope)
    }

    fun setReact(isLike: Boolean, isDislike: Boolean, postId: String) =
            useCase.setReact(isLike, isDislike, postId)

    fun deletePost(postId: Int) = useCase.deleteGroupPost(postId)

    fun setBell(postId: String) = useCase.setBell(postId)

    fun deleteBell(postId: String) = useCase.deleteBell(postId)

    fun editPost(groupPostEntity: GroupPostEntity.PostEntity) =
            useCase.editPost(groupPostEntity)
}