package com.intergroupapplication.presentation.feature.grouplist.di

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.appodeal.ads.Appodeal
import com.appodeal.ads.native_ad.views.NativeAdViewAppWall
import com.clockbyte.admobadapter.bannerads.AdmobBannerRecyclerAdapterWrapper
import com.clockbyte.admobadapter.bannerads.BannerAdViewWrappingStrategy
import com.google.android.gms.ads.AdView
import com.intergroupapplication.R
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.presentation.base.FrescoImageLoader
import com.intergroupapplication.presentation.base.ImageLoader
import com.intergroupapplication.presentation.base.adapter.PagingLoadingAdapter
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.feature.grouplist.adapter.GroupListAdapter3
import com.intergroupapplication.presentation.feature.grouplist.other.GroupsFragment
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.manager.DialogProvider
import com.intergroupapplication.presentation.manager.ToastManager
import dagger.Module
import dagger.Provides

@Module
class GroupsViewModule {

    @PerFragment
    @Provides
    fun provideFrescoImageLoader(context: Context): ImageLoader =
            FrescoImageLoader(context)

    @PerFragment
    @Provides
    fun provideImageLoadingDelegate(imageLoader: ImageLoader): ImageLoadingDelegate =
            ImageLoadingDelegate(imageLoader)

    @PerFragment
    @Provides
    fun provideDialogManager(activity: GroupsFragment): DialogManager =
            DialogManager(activity.requireActivity().supportFragmentManager)

    @PerFragment
    @Provides
    fun dialogDelegate(dialogManager: DialogManager, dialogProvider: DialogProvider, toastManager: ToastManager,
                       context: Context)
            : DialogDelegate =
            DialogDelegate(dialogManager, dialogProvider, toastManager, context)

    @PerFragment
    @Provides
    fun provideGroupPostEntityDiffUtilCallback() = object : DiffUtil.ItemCallback<GroupEntity>() {
        override fun areItemsTheSame(oldItem: GroupEntity, newItem: GroupEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: GroupEntity, newItem: GroupEntity) = oldItem == newItem
    }

    @PerFragment
    @Provides
    fun provideGroupListNew1(diffUtil: DiffUtil.ItemCallback<GroupEntity>,
                          imageLoadingDelegate: ImageLoadingDelegate): GroupListAdapter3 {
        val adapter = GroupListAdapter3(diffUtil, imageLoadingDelegate)
        adapter.withLoadStateFooter(PagingLoadingAdapter{adapter.retry()})
        return adapter
    }

    @PerFragment
    @Provides
    fun provideAdmobBammerAdapter(context: Context,
                                  groupsAdapter: GroupListAdapter3,
                                  activity: GroupsFragment, userSession: UserSession):
            AdmobBannerRecyclerAdapterWrapper =
            AdmobBannerRecyclerAdapterWrapper.builder(context)
                    .setLimitOfAds(userSession.countAd?.limitOfAdsGroups ?: 20)
                    .setFirstAdIndex(userSession.countAd?.firstAdIndexGroups ?: 4)
                    .setAdViewWrappingStrategy(object : BannerAdViewWrappingStrategy() {
                        override fun addAdViewToWrapper(wrapper: ViewGroup, ad: AdView) {
                            val container = wrapper.findViewById(R.id.adsCardView) as ViewGroup
                            container.removeAllViews()
                            //container.addView(ad)
                            val t = Appodeal.getNativeAds(1)
                            if (t.size>0) {
                                val nativeAdView = NativeAdViewAppWall(activity.requireActivity(), t[0], GROUPS)
                                container.addView(nativeAdView)
                            }
                        }
                        override fun getAdViewWrapper(parent: ViewGroup?): ViewGroup {
                            return LayoutInflater.from(parent?.context).inflate(R.layout.layout_appodeal_groups,
                                    parent, false) as ViewGroup
                        }
                    })
                    .setNoOfDataBetweenAds(userSession.countAd?.noOfDataBetweenAdsGroups ?: 7)
                    .setAdapter(groupsAdapter)
                    .build()


}
