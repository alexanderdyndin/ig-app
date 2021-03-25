package com.intergroupapplication.presentation.feature.commentsdetails.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import androidx.paging.rxjava2.cachedIn
import com.appodeal.ads.Appodeal
import com.appodeal.ads.NativeAd
import com.intergroupapplication.domain.usecase.CommentsUseCase
import com.intergroupapplication.domain.usecase.PostsUseCase
import com.intergroupapplication.presentation.feature.commentsdetails.adapter.CommentsAdapter
import com.intergroupapplication.presentation.feature.commentsdetails.other.CommentEntityUI
import com.intergroupapplication.presentation.feature.news.adapter.NewsAdapter
import com.intergroupapplication.presentation.feature.news.other.GroupPostEntityUI
import io.reactivex.Flowable
import javax.inject.Inject

class CommentsViewModel @Inject constructor(private val commentsUseCase: CommentsUseCase,
                                            private val postsUseCase: PostsUseCase): ViewModel() {

    private val nativeAdItem: NativeAd?
        get() {
            val ads = Appodeal.getNativeAds(1)
            return if (ads.isNotEmpty()) ads[0] else null
        }

    fun fetchComments(postId: String): Flowable<PagingData<CommentEntityUI>> {
        return commentsUseCase
                .getComments(postId)
                .map { pagingData ->
                    var i = -CommentsAdapter.AD_FIRST - 1
                    pagingData.map {
                        CommentEntityUI.CommentEntity(
                                it.id,
                                it.text,
                                it.date,
                                it.commentOwner,
                                it.answerTo
                        )
                    }
                            .insertSeparators<CommentEntityUI.CommentEntity, CommentEntityUI>
                            { before: CommentEntityUI.CommentEntity?, after: CommentEntityUI.CommentEntity? ->
                                i++
                                when {
                                    before == null -> null
                                    after == null -> null
                                    else -> if ( i % CommentsAdapter.AD_FREQ == 0 && i >= 0) {
                                        var nativeAd: NativeAd?
                                        if (nativeAdItem.also { nativeAd = it } != null) {
                                            CommentEntityUI.AdEntity(i, nativeAd)
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