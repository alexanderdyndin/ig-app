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
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.repository.PhotoRepository
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.domain.gateway.AwsUploadingGateway
import com.intergroupapplication.domain.gateway.PhotoGateway
import com.intergroupapplication.presentation.base.FrescoImageLoader
import com.intergroupapplication.presentation.base.ImageLoader
import com.intergroupapplication.presentation.base.ImageUploader
import com.intergroupapplication.presentation.base.adapter.PagingLoadingAdapter
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.delegate.ImageUploadingDelegate
import com.intergroupapplication.presentation.feature.grouplist.adapter.GroupListAdapter3
import com.intergroupapplication.presentation.feature.grouplist.view.GroupListFragment
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.manager.DialogProvider
import com.intergroupapplication.presentation.manager.ToastManager
import com.yalantis.ucrop.UCrop
import dagger.Module
import dagger.Provides
import javax.inject.Named

const val GROUPS = "Groups"

@Module
class GroupListViewModule {



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
    fun providePhotoGateway(activity: GroupListFragment, cropOptions: UCrop.Options,
                            api: AppApi, awsUploadingGateway: AwsUploadingGateway): PhotoGateway =
            PhotoRepository(activity.requireActivity(), cropOptions, api, awsUploadingGateway)

    @PerFragment
    @Provides
    fun provideImageUploader(photoGateway: PhotoGateway): ImageUploader =
            ImageUploadingDelegate(photoGateway)

    @PerFragment
    @Provides
    fun provideDialogManager(activity: GroupListFragment): DialogManager =
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
    @Named("all")
    fun provideGroupListNew1(diffUtil: DiffUtil.ItemCallback<GroupEntity>,
                             imageLoadingDelegate: ImageLoadingDelegate): GroupListAdapter3 {
        val adapter = GroupListAdapter3(diffUtil, imageLoadingDelegate)
        adapter.withLoadStateFooter(PagingLoadingAdapter{adapter.retry()})
        return adapter
    }

    @PerFragment
    @Provides
    @Named("subscribed")
    fun provideGroupListNew2(diffUtil: DiffUtil.ItemCallback<GroupEntity>,
                             imageLoadingDelegate: ImageLoadingDelegate): GroupListAdapter3 {
        val adapter = GroupListAdapter3(diffUtil, imageLoadingDelegate)
        adapter.withLoadStateFooter(PagingLoadingAdapter{adapter.retry()})
        return adapter
    }

    @PerFragment
    @Provides
    @Named("owned")
    fun provideGroupListNew3(diffUtil: DiffUtil.ItemCallback<GroupEntity>,
                             imageLoadingDelegate: ImageLoadingDelegate): GroupListAdapter3 {
        val adapter = GroupListAdapter3(diffUtil, imageLoadingDelegate)
        adapter.withLoadStateFooter(PagingLoadingAdapter{adapter.retry()})
        return adapter
    }

    @PerFragment
    @Provides
    @Named("all")
    fun provideAdmobBammerAdapter1(context: Context,
                                   @Named("all") groupsAdapter: GroupListAdapter3,
                                  activity: GroupListFragment, userSession: UserSession):
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

    @PerFragment
    @Provides
    @Named("subscribed")
    fun provideAdmobBammerAdapter2(context: Context,
                                   @Named("subscribed") groupsAdapter: GroupListAdapter3,
                                  activity: GroupListFragment, userSession: UserSession):
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

    @PerFragment
    @Provides
    @Named("owned")
    fun provideAdmobBammerAdapter3(context: Context,
                                   @Named("owned") groupsAdapter: GroupListAdapter3,
                                  activity: GroupListFragment, userSession: UserSession):
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
