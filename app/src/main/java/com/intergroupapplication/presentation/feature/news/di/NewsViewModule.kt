package com.intergroupapplication.presentation.feature.news.di

import android.content.Context
import androidx.recyclerview.widget.ConcatAdapter
import com.danikula.videocache.HttpProxyCacheServer
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.di.qualifier.Footer
import com.intergroupapplication.di.qualifier.Header
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.presentation.base.FrescoImageLoader
import com.intergroupapplication.presentation.base.ImageLoader
import com.intergroupapplication.presentation.base.adapter.PagingLoadingAdapter
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.feature.news.adapter.NewsAdapter
import com.intergroupapplication.presentation.feature.news.view.NewsFragment
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.provider.DialogProvider
import dagger.Module
import dagger.Provides


const val NEWS = "News"

@Module
class NewsViewModule {

    @PerFragment
    @Provides
    fun provideFrescoImageLoader(): ImageLoader =
        FrescoImageLoader()


    @PerFragment
    @Provides
    fun provideImageLoadingDelegate(imageLoader: ImageLoader): ImageLoadingDelegate =
            ImageLoadingDelegate(imageLoader)

    @PerFragment
    @Provides
    fun provideDialogManager(fragment: NewsFragment): DialogManager =
            DialogManager(fragment.requireActivity().supportFragmentManager)

    @PerFragment
    @Provides
    fun dialogDelegate(
        dialogManager: DialogManager, dialogProvider: DialogProvider,
        context: Context
    )
            : DialogDelegate =
        DialogDelegate(dialogManager, dialogProvider, context)

    @PerFragment
    @Provides
    fun provideNewsAdapter(imageLoadingDelegate: ImageLoadingDelegate,
                           userSession: UserSession, cacheServer: HttpProxyCacheServer): NewsAdapter {
        if (userSession.isAdEnabled) {
            NewsAdapter.AD_TYPE = userSession.countAd?.limitOfAdsNews ?: 1
            NewsAdapter.AD_FREQ = userSession.countAd?.noOfDataBetweenAdsNews ?: 7
            NewsAdapter.AD_FIRST = userSession.countAd?.firstAdIndexNews ?: 3
        } else {
            NewsAdapter.AD_TYPE = 1
            NewsAdapter.AD_FREQ = 999
            NewsAdapter.AD_FIRST = 999
        }
        return NewsAdapter(imageLoadingDelegate, cacheServer)
    }

    @PerFragment
    @Provides
    @Footer
    fun provideFooterAdapter(newsAdapter: NewsAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { newsAdapter.retry() }
    }

    @PerFragment
    @Provides
    @Header
    fun provideHeaderAdapter(newsAdapter: NewsAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { newsAdapter.retry() }
    }

    @PerFragment
    @Provides
    fun provideConcatAdapter(newsAdapter: NewsAdapter,
                             @Footer footerAdapter: PagingLoadingAdapter,
                             @Header headerAdapter: PagingLoadingAdapter
                             ): ConcatAdapter {
        return newsAdapter.withLoadStateHeaderAndFooter(headerAdapter, footerAdapter)
    }

//    @PerFragment
//    @Provides
//    fun provideAdmobBammerAdapter(context: Context,
//                                  newsAdapter: ConcatAdapter, activity: NewsFragment,
//                                  userSession: UserSession): AdmobBannerRecyclerAdapterWrapper =
//            AdmobBannerRecyclerAdapterWrapper.builder(context)
//                    .setLimitOfAds(userSession.countAd?.limitOfAdsNews ?: 20)
//                    .setFirstAdIndex(userSession.countAd?.firstAdIndexNews ?: 10)
//                    .setAdViewWrappingStrategy(object : BannerAdViewWrappingStrategy() {
//                        override fun addAdViewToWrapper(wrapper: ViewGroup, ad: AdView) {
//                            val container = wrapper.findViewById(R.id.adsCardView) as ViewGroup
//                            container.removeAllViews()
//                            val t = Appodeal.getNativeAds(1)
//                            if (t.size>0) {
//                                val nativeAdView = NativeAdViewAppWall(activity.requireActivity(), t[0], NEWS)
//                                container.addView(nativeAdView)
//                            }
//                        }
//                        override fun getAdViewWrapper(parent: ViewGroup?): ViewGroup {
//                            return LayoutInflater.from(parent?.context).inflate(R.layout.layout_admob_news,
//                                    parent, false) as ViewGroup
//                        }
//                    })
//                    .setNoOfDataBetweenAds(userSession.countAd?.noOfDataBetweenAdsNews ?: 7)
//                    .setAdapter(newsAdapter)
//                    .build()
//
//    @PerFragment
//    @Provides
//    fun provideLinearLayoutManager(fragment: NewsFragment): RecyclerView.LayoutManager =
//            LinearLayoutManager(fragment.context, LinearLayoutManager.VERTICAL, false)

}
