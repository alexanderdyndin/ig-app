package com.intergroupapplication.presentation.feature.commentsdetails.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import androidx.paging.rxjava2.cachedIn
import com.appodeal.ads.Appodeal
import com.appodeal.ads.NativeAd
import com.intergroupapplication.domain.entity.CommentEntity
import com.intergroupapplication.domain.usecase.CommentsUseCase
import com.intergroupapplication.domain.usecase.PostsUseCase
import com.intergroupapplication.presentation.feature.commentsdetails.adapter.CommentsAdapter
import io.reactivex.Flowable
import javax.inject.Inject

class CommentsViewModel @Inject constructor(private val commentsUseCase: CommentsUseCase,
                                            private val postsUseCase: PostsUseCase): ViewModel() {

    private val nativeAdItem: NativeAd?
        get() {
            val ads = Appodeal.getNativeAds(1)
            return if (ads.isNotEmpty()) ads[0] else null
        }

    fun fetchComments(postId: String): Flowable<PagingData<CommentEntity>> {
        return commentsUseCase
                .getComments(postId)
                .map { pagingData ->
                    var i = -CommentsAdapter.AD_FIRST - 1
                    pagingData.map {
                        it as CommentEntity.Comment
                    }
                            .insertSeparators<CommentEntity.Comment, CommentEntity>
                            { before: CommentEntity.Comment?, after: CommentEntity.Comment? ->
                                i++
                                when {
                                    before == null -> null
                                    after == null -> null
                                    else -> if ( i % CommentsAdapter.AD_FREQ == 0 && i >= 0) {
                                        var nativeAd: NativeAd?
                                        if (nativeAdItem.also { nativeAd = it } != null) {
                                            CommentEntity.AdEntity(i, nativeAd)
                                        } else null
                                    } else null
                                }
                            }
                }
                .cachedIn(viewModelScope)
    }

    fun setReact(isLike: Boolean, isDislike: Boolean, postId: String) =
            postsUseCase.setReact(isLike, isDislike, postId)
}