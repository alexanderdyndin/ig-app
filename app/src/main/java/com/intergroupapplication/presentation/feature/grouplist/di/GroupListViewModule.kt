package com.intergroupapplication.presentation.feature.grouplist.di

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.appodeal.ads.Appodeal
import com.appodeal.ads.native_ad.views.NativeAdViewAppWall
import com.clockbyte.admobadapter.bannerads.AdmobBannerRecyclerAdapterWrapper
import com.clockbyte.admobadapter.bannerads.BannerAdViewWrappingStrategy
import com.google.android.gms.ads.AdView
import com.intergroupapplication.R
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.feature.grouplist.adapter.GroupListAdapter
import com.intergroupapplication.presentation.feature.grouplist.view.GroupListFragment
import com.intergroupapplication.presentation.feature.navigation.view.NavigationActivity
import com.intergroupapplication.presentation.feature.news.adapter.NewsAdapter
import dagger.Module
import dagger.Provides
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import javax.inject.Named

@Module
class GroupListViewModule {

    @PerFragment
    @Provides
    fun provideGroupPostEntityDiffUtilCallback() = object : DiffUtil.ItemCallback<GroupEntity>() {
        override fun areItemsTheSame(oldItem: GroupEntity, newItem: GroupEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: GroupEntity, newItem: GroupEntity) = oldItem == newItem
    }

    @PerFragment
    @Provides
    @Named("AdapterAll")
    fun provideGroupList1(diffUtil: DiffUtil.ItemCallback<GroupEntity>,
                          imageLoadingDelegate: ImageLoadingDelegate): GroupListAdapter =
            GroupListAdapter(diffUtil, imageLoadingDelegate)

    @PerFragment
    @Provides
    @Named("AdapterSub")
    fun provideGroupList2(diffUtil: DiffUtil.ItemCallback<GroupEntity>,
                          imageLoadingDelegate: ImageLoadingDelegate): GroupListAdapter =
            GroupListAdapter(diffUtil, imageLoadingDelegate)

    @PerFragment
    @Provides
    @Named("AdapterAdm")
    fun provideGroupList3(diffUtil: DiffUtil.ItemCallback<GroupEntity>,
                          imageLoadingDelegate: ImageLoadingDelegate): GroupListAdapter =
            GroupListAdapter(diffUtil, imageLoadingDelegate)

    @PerFragment
    @Provides
    @Named("AdapterAll")
    fun provideAdmobBammerAdapter(context: Context,
                                  @Named("AdapterAll") groupsAdapter: GroupListAdapter, activity: NavigationActivity): AdmobBannerRecyclerAdapterWrapper =
            AdmobBannerRecyclerAdapterWrapper.builder(context)
                    .setLimitOfAds(20)
                    .setFirstAdIndex(4)
                    .setAdViewWrappingStrategy(object : BannerAdViewWrappingStrategy() {
                        override fun addAdViewToWrapper(wrapper: ViewGroup, ad: AdView) {
                            val container = wrapper.findViewById(R.id.adsCardView) as ViewGroup
                            container.removeAllViews()
                            //container.addView(ad)
                            val t = Appodeal.getNativeAds(1)
                            if (t.size>0) {
                                val nativeAdView = NativeAdViewAppWall(activity, t[0])
                                container.addView(nativeAdView)
                            }
                        }
                        override fun getAdViewWrapper(parent: ViewGroup?): ViewGroup {
                            return LayoutInflater.from(parent?.context).inflate(R.layout.layout_appodeal_groups,
                                    parent, false) as ViewGroup
                        }
                    })
                    .setNoOfDataBetweenAds(7)
                    .setAdapter(groupsAdapter)
                    .build()

    @PerFragment
    @Provides
    @Named("AdapterSub")
    fun provideAdmobBammerAdapter2(context: Context,
                                   @Named("AdapterSub") groupsAdapter: GroupListAdapter, activity: NavigationActivity): AdmobBannerRecyclerAdapterWrapper {
        return AdmobBannerRecyclerAdapterWrapper.builder(context)
                .setLimitOfAds(20)
                .setFirstAdIndex(4)
                .setAdViewWrappingStrategy(object : BannerAdViewWrappingStrategy() {
                    override fun addAdViewToWrapper(wrapper: ViewGroup, ad: AdView) {
                        val container = wrapper.findViewById(R.id.adsCardView) as ViewGroup
                        container.removeAllViews()
                        //container.addView(ad)
                        val t = Appodeal.getNativeAds(1)
                        if (t.size > 0) {
                            val nativeAdView = NativeAdViewAppWall(activity, t[0])
                            container.addView(nativeAdView)
                        }
                    }

                    override fun getAdViewWrapper(parent: ViewGroup?): ViewGroup {
                        return LayoutInflater.from(parent?.context).inflate(R.layout.layout_appodeal_groups,
                                parent, false) as ViewGroup
                    }
                })
                .setNoOfDataBetweenAds(7)
                .setAdapter(groupsAdapter)
                .build()
    }

    @PerFragment
    @Provides
    @Named("AdapterAdm")
    fun provideAdmobBammerAdapter3(context: Context,
                                   @Named("AdapterAdm") groupsAdapter: GroupListAdapter, activity: NavigationActivity): AdmobBannerRecyclerAdapterWrapper =
            AdmobBannerRecyclerAdapterWrapper.builder(context)
                    .setLimitOfAds(20)
                    .setFirstAdIndex(4)
                    .setAdViewWrappingStrategy(object : BannerAdViewWrappingStrategy() {
                        override fun addAdViewToWrapper(wrapper: ViewGroup, ad: AdView) {
                            val container = wrapper.findViewById(R.id.adsCardView) as ViewGroup
                            container.removeAllViews()
                            //container.addView(ad)
                            val t = Appodeal.getNativeAds(1)
                            if (t.size>0) {
                                val nativeAdView = NativeAdViewAppWall(activity, t[0])
                                container.addView(nativeAdView)
                            }
                        }
                        override fun getAdViewWrapper(parent: ViewGroup?): ViewGroup {
                            return LayoutInflater.from(parent?.context).inflate(R.layout.layout_appodeal_groups,
                                    parent, false) as ViewGroup
                        }
                    })
                    .setNoOfDataBetweenAds(7)
                    .setAdapter(groupsAdapter)
                    .build()

    @PerFragment
    @Provides
    fun provideSupportAppNavigator(activity: NavigationActivity): SupportAppNavigator =
            SupportAppNavigator(activity, 0)

    @PerFragment
    @Provides
    fun provideLinearLayoutManager(fragment: GroupListFragment): LinearLayoutManager =
            LinearLayoutManager(fragment.context, LinearLayoutManager.VERTICAL, false)

}
