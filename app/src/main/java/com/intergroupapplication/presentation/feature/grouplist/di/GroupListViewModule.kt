package com.intergroupapplication.presentation.feature.grouplist.di

import android.content.Context
import androidx.recyclerview.widget.ConcatAdapter
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.repository.PhotoRepository
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.domain.gateway.AwsUploadingGateway
import com.intergroupapplication.domain.gateway.PhotoGateway
import com.intergroupapplication.presentation.base.FrescoImageLoader
import com.intergroupapplication.presentation.base.ImageLoader
import com.intergroupapplication.presentation.base.ImageUploader
import com.intergroupapplication.presentation.base.adapter.PagingLoadingAdapter
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.delegate.ImageUploadingDelegate
import com.intergroupapplication.presentation.feature.grouplist.adapter.GroupListAdapter
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

//    @PerFragment
//    @Provides
//    fun provideGroupPostEntityDiffUtilCallback() = object : DiffUtil.ItemCallback<GroupEntity>() {
//        override fun areItemsTheSame(oldItem: GroupEntity, newItem: GroupEntity) = oldItem.id == newItem.id
//        override fun areContentsTheSame(oldItem: GroupEntity, newItem: GroupEntity) = oldItem == newItem
//    }

    @PerFragment
    @Provides
    @Named("footerAll")
    fun provideFooterAdapterAll(@Named("all") groupListAdapter: GroupListAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { groupListAdapter.retry() }
    }

    @PerFragment
    @Provides
    @Named("headerAll")
    fun provideHeaderAdapterAll(@Named("all") groupListAdapter: GroupListAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { groupListAdapter.retry() }
    }

    @PerFragment
    @Provides
    @Named("footerSub")
    fun provideFooterAdapterSub(@Named("subscribed") groupListAdapter: GroupListAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { groupListAdapter.retry() }
    }

    @PerFragment
    @Provides
    @Named("headerSub")
    fun provideHeaderAdapterSub(@Named("subscribed") groupListAdapter: GroupListAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { groupListAdapter.retry() }
    }

    @PerFragment
    @Provides
    @Named("footerAdm")
    fun provideFooterAdapterAdm(@Named("owned") groupListAdapter: GroupListAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { groupListAdapter.retry() }
    }

    @PerFragment
    @Provides
    @Named("headerAdm")
    fun provideHeaderAdapterAdm(@Named("owned") groupListAdapter: GroupListAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { groupListAdapter.retry() }
    }

    @PerFragment
    @Provides
    @Named("all")
    fun provideGroupListAll(imageLoadingDelegate: ImageLoadingDelegate,
                            userSession: UserSession
    ): GroupListAdapter {
        GroupListAdapter.AD_FIRST = userSession.countAd?.firstAdIndexGroups ?: 5
        GroupListAdapter.AD_FREQ = userSession.countAd?.noOfDataBetweenAdsGroups ?: 5
        GroupListAdapter.AD_TYPE = userSession.countAd?.limitOfAdsGroups ?: 1
        return GroupListAdapter(imageLoadingDelegate)
    }


    @PerFragment
    @Provides
    @Named("subscribed")
    fun provideGroupListSub(imageLoadingDelegate: ImageLoadingDelegate): GroupListAdapter {
        return GroupListAdapter(imageLoadingDelegate)
    }

    @PerFragment
    @Provides
    @Named("owned")
    fun provideConcatGroupListAdm(imageLoadingDelegate: ImageLoadingDelegate): GroupListAdapter {
        return GroupListAdapter(imageLoadingDelegate)
    }

    @PerFragment
    @Provides
    @Named("all")
    fun provideConcatListAll(@Named("all") groupListAdapter: GroupListAdapter,
                             @Named("footerAll") pagingFooter: PagingLoadingAdapter,
                             @Named("headerAll") pagingHeader: PagingLoadingAdapter,
    ): ConcatAdapter {
        return groupListAdapter.withLoadStateHeaderAndFooter(pagingHeader, pagingFooter)
    }


    @PerFragment
    @Provides
    @Named("subscribed")
    fun provideConcatListSub(@Named("subscribed") groupListAdapter: GroupListAdapter,
                             @Named("footerSub") pagingFooter: PagingLoadingAdapter,
                             @Named("headerSub") pagingHeader: PagingLoadingAdapter,
    ): ConcatAdapter {
        return groupListAdapter.withLoadStateHeaderAndFooter(pagingHeader, pagingFooter)
    }

    @PerFragment
    @Provides
    @Named("owned")
    fun provideGroupListAdm(@Named("owned") groupListAdapter: GroupListAdapter,
                            @Named("footerAdm") pagingFooter: PagingLoadingAdapter,
                            @Named("headerAdm") pagingHeader: PagingLoadingAdapter,
    ): ConcatAdapter {
        return groupListAdapter.withLoadStateHeaderAndFooter(pagingHeader, pagingFooter)
    }



//    @PerFragment
//    @Provides
//    @Named("all")
//    fun provideAdmobBammerAdapter1(context: Context,
//                                   @Named("all") groupsAdapter: GroupListAdapter3,
//                                  activity: GroupListFragment, userSession: UserSession):
//            AdmobBannerRecyclerAdapterWrapper =
//            AdmobBannerRecyclerAdapterWrapper.builder(context)
//                    .setLimitOfAds(userSession.countAd?.limitOfAdsGroups ?: 20)
//                    .setFirstAdIndex(userSession.countAd?.firstAdIndexGroups ?: 4)
//                    .setAdViewWrappingStrategy(object : BannerAdViewWrappingStrategy() {
//                        override fun addAdViewToWrapper(wrapper: ViewGroup, ad: AdView) {
//                            val container = wrapper.findViewById(R.id.adsCardView) as ViewGroup
//                            container.removeAllViews()
//                            //container.addView(ad)
//                            val t = Appodeal.getNativeAds(1)
//                            if (t.size>0) {
//                                val nativeAdView = NativeAdViewAppWall(activity.requireActivity(), t[0], GROUPS)
//                                container.addView(nativeAdView)
//                            }
//                        }
//                        override fun getAdViewWrapper(parent: ViewGroup?): ViewGroup {
//                            return LayoutInflater.from(parent?.context).inflate(R.layout.layout_appodeal_groups,
//                                    parent, false) as ViewGroup
//                        }
//                    })
//                    .setNoOfDataBetweenAds(userSession.countAd?.noOfDataBetweenAdsGroups ?: 7)
//                    .setAdapter(groupsAdapter)
//                    .build()
//
//    @PerFragment
//    @Provides
//    @Named("subscribed")
//    fun provideAdmobBammerAdapter2(context: Context,
//                                   @Named("subscribed") groupsAdapter: GroupListAdapter3,
//                                  activity: GroupListFragment, userSession: UserSession):
//            AdmobBannerRecyclerAdapterWrapper =
//            AdmobBannerRecyclerAdapterWrapper.builder(context)
//                    .setLimitOfAds(userSession.countAd?.limitOfAdsGroups ?: 20)
//                    .setFirstAdIndex(userSession.countAd?.firstAdIndexGroups ?: 4)
//                    .setAdViewWrappingStrategy(object : BannerAdViewWrappingStrategy() {
//                        override fun addAdViewToWrapper(wrapper: ViewGroup, ad: AdView) {
//                            val container = wrapper.findViewById(R.id.adsCardView) as ViewGroup
//                            container.removeAllViews()
//                            //container.addView(ad)
//                            val t = Appodeal.getNativeAds(1)
//                            if (t.size>0) {
//                                val nativeAdView = NativeAdViewAppWall(activity.requireActivity(), t[0], GROUPS)
//                                container.addView(nativeAdView)
//                            }
//                        }
//                        override fun getAdViewWrapper(parent: ViewGroup?): ViewGroup {
//                            return LayoutInflater.from(parent?.context).inflate(R.layout.layout_appodeal_groups,
//                                    parent, false) as ViewGroup
//                        }
//                    })
//                    .setNoOfDataBetweenAds(userSession.countAd?.noOfDataBetweenAdsGroups ?: 7)
//                    .setAdapter(groupsAdapter)
//                    .build()
//
//    @PerFragment
//    @Provides
//    @Named("owned")
//    fun provideAdmobBammerAdapter3(context: Context,
//                                   @Named("owned") groupsAdapter: GroupListAdapter3,
//                                  activity: GroupListFragment, userSession: UserSession):
//            AdmobBannerRecyclerAdapterWrapper =
//            AdmobBannerRecyclerAdapterWrapper.builder(context)
//                    .setLimitOfAds(userSession.countAd?.limitOfAdsGroups ?: 20)
//                    .setFirstAdIndex(userSession.countAd?.firstAdIndexGroups ?: 4)
//                    .setAdViewWrappingStrategy(object : BannerAdViewWrappingStrategy() {
//                        override fun addAdViewToWrapper(wrapper: ViewGroup, ad: AdView) {
//                            val container = wrapper.findViewById(R.id.adsCardView) as ViewGroup
//                            container.removeAllViews()
//                            //container.addView(ad)
//                            val t = Appodeal.getNativeAds(1)
//                            if (t.size>0) {
//                                val nativeAdView = NativeAdViewAppWall(activity.requireActivity(), t[0], GROUPS)
//                                container.addView(nativeAdView)
//                            }
//                        }
//                        override fun getAdViewWrapper(parent: ViewGroup?): ViewGroup {
//                            return LayoutInflater.from(parent?.context).inflate(R.layout.layout_appodeal_groups,
//                                    parent, false) as ViewGroup
//                        }
//                    })
//                    .setNoOfDataBetweenAds(userSession.countAd?.noOfDataBetweenAdsGroups ?: 7)
//                    .setAdapter(groupsAdapter)
//                    .build()

}
