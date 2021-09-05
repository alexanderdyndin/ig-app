package com.intergroupapplication.presentation.feature.news.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import androidx.paging.rxjava2.cachedIn
import com.appodeal.ads.Appodeal
import com.appodeal.ads.NativeAd
import com.intergroupapplication.domain.entity.NewsEntity
import com.intergroupapplication.domain.usecase.PostsUseCase
import com.intergroupapplication.presentation.feature.news.adapter.NewsAdapter
import io.reactivex.Flowable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber
import javax.inject.Inject

class NewsViewModel @Inject constructor(private val useCase: PostsUseCase) : ViewModel() {

    var nativeAdCount = 0

    private val nativeAdItem: NativeAd?
        get() {
            val ads = Appodeal.getNativeAds(1)
            return if (ads.isNotEmpty()) ads[0] else null
        }

    @ExperimentalCoroutinesApi
    fun getNews(): Flowable<PagingData<NewsEntity>> {
        return useCase.getNews()
            .map { pagingData ->
                var i = -NewsAdapter.AD_FIRST - 1
                pagingData.map {
                    it as NewsEntity.Post
                }
                    .insertSeparators<NewsEntity.Post, NewsEntity>
                    { before: NewsEntity.Post?, after: NewsEntity.Post? ->
                        i++
                        when {
                            before == null -> null
                            after == null -> null
                            else -> if (i % NewsAdapter.AD_FREQ == 0 && i >= 0) {
                                var nativeAd: NativeAd?
                                Timber.d("trying to get news ad, avaible ad:" +
                                        "${Appodeal.getAvailableNativeAdsCount()}")
                                if (nativeAdItem.also { nativeAd = it } != null) {
                                    NewsEntity.AdEntity(i, nativeAd)
                                } else null
                            } else null
                        }
                    }
            }
            .cachedIn(viewModelScope)
    }

    fun setReact(isLike: Boolean, isDislike: Boolean, postId: String) =
        useCase.setReact(isLike, isDislike, postId)

    fun deletePost(postId: Int) = useCase.deleteNewsPost(postId)

    fun setBell(postId: String) = useCase.setBell(postId)

    fun deleteBell(postId: String) = useCase.deleteBell(postId)

    fun sendComplaint(postId: Int) = useCase.sendComplaint(postId)

}
