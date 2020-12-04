package com.intergroupapplication.presentation.feature.news.di

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appodeal.ads.Appodeal
import com.appodeal.ads.native_ad.views.NativeAdViewAppWall
import com.clockbyte.admobadapter.bannerads.AdmobBannerRecyclerAdapterWrapper
import com.clockbyte.admobadapter.bannerads.BannerAdViewWrappingStrategy
import com.google.android.gms.ads.AdView
import com.intergroupapplication.BuildConfig
import com.intergroupapplication.R
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.feature.navigation.view.NavigationActivity
import com.intergroupapplication.presentation.feature.news.adapter.NewsAdapter
import com.intergroupapplication.presentation.feature.news.view.NewsFragment
import dagger.Module
import dagger.Provides
import ru.terrakok.cicerone.android.support.SupportAppNavigator


@Module
class NewsViewModule {

    @PerFragment
    @Provides
    fun provideGroupPostEntityDiffUtilCallback() = object : DiffUtil.ItemCallback<GroupPostEntity>() {
        override fun areItemsTheSame(oldItem: GroupPostEntity, newItem: GroupPostEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: GroupPostEntity, newItem: GroupPostEntity) = oldItem == newItem
    }


    @PerFragment
    @Provides
    fun provideNewsAdapter(diffUtil: DiffUtil.ItemCallback<GroupPostEntity>,
                           imageLoadingDelegate: ImageLoadingDelegate): NewsAdapter =
            NewsAdapter(diffUtil, imageLoadingDelegate)

    @PerFragment
    @Provides
    fun provideAdmobBammerAdapter(context: Context,
                                  newsAdapter: NewsAdapter, activity: NavigationActivity): AdmobBannerRecyclerAdapterWrapper =
            AdmobBannerRecyclerAdapterWrapper.builder(context)
                    .setLimitOfAds(10)
                    .setFirstAdIndex(4)
                    .setAdViewWrappingStrategy(object : BannerAdViewWrappingStrategy() {
                        override fun addAdViewToWrapper(wrapper: ViewGroup, ad: AdView) {
                            val container = wrapper.findViewById(R.id.adsCardView) as ViewGroup
                            container.removeAllViews()
                            container.addView(ad)
                            val t = Appodeal.getNativeAds(1)
                            if (t.size>0) {
                                val nativeAdView = NativeAdViewAppWall(activity, t[0])
                                container.addView(nativeAdView)
                            }
                        }
                        override fun getAdViewWrapper(parent: ViewGroup?): ViewGroup {
                            return LayoutInflater.from(parent?.context).inflate(R.layout.layout_admob_news,
                                    parent, false) as ViewGroup
                        }
                    })
                    .setNoOfDataBetweenAds(7)
                    //.setSingleAdUnitId(BuildConfig.BANNER_AD_UNIT_ID)
                    //.setTestDeviceIds(arrayOf("BA4CB07CBCAA1F64824EE76EC089BA5A"))
                    .setAdapter(newsAdapter)
                    .build()

    @PerFragment
    @Provides
    fun provideSupportAppNavigator(activity: NavigationActivity): SupportAppNavigator =
            SupportAppNavigator(activity, 0)

    @PerFragment
    @Provides
    fun provideLinearLayoutManager(fragment: NewsFragment): RecyclerView.LayoutManager =
            LinearLayoutManager(fragment.context, LinearLayoutManager.VERTICAL, false)

}
