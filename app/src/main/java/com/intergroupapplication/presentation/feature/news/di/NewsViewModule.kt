package com.intergroupapplication.presentation.feature.news.di

import android.content.Context
import android.view.LayoutInflater
import android.view.View
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
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.repository.PhotoRepository
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.di.scope.PerActivity
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.gateway.AwsUploadingGateway
import com.intergroupapplication.domain.gateway.PhotoGateway
import com.intergroupapplication.presentation.base.FrescoImageLoader
import com.intergroupapplication.presentation.base.ImageLoader
import com.intergroupapplication.presentation.base.ImageUploader
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.delegate.ImageUploadingDelegate
import com.intergroupapplication.presentation.feature.grouplist.view.GroupListFragment
import com.intergroupapplication.presentation.feature.news.adapter.NewsAdapter
import com.intergroupapplication.presentation.feature.news.view.NewsFragment
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.manager.DialogProvider
import com.intergroupapplication.presentation.manager.ToastManager
import com.yalantis.ucrop.UCrop
import dagger.Module
import dagger.Provides


const val NEWS = "News"

@Module
class NewsViewModule {

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
    fun providePhotoGateway(activity: NewsFragment, cropOptions: UCrop.Options,
                            api: AppApi, awsUploadingGateway: AwsUploadingGateway): PhotoGateway =
            PhotoRepository(activity.requireActivity(), cropOptions, api, awsUploadingGateway)

    @PerFragment
    @Provides
    fun provideImageUploader(photoGateway: PhotoGateway): ImageUploader =
            ImageUploadingDelegate(photoGateway)

    @PerFragment
    @Provides
    fun provideDialogManager(activity: NewsFragment): DialogManager =
            DialogManager(activity.requireActivity().supportFragmentManager)

    @PerFragment
    @Provides
    fun dialogDelegate(dialogManager: DialogManager, dialogProvider: DialogProvider, toastManager: ToastManager,
                       context: Context)
            : DialogDelegate =
            DialogDelegate(dialogManager, dialogProvider, toastManager, context)

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
                                  newsAdapter: NewsAdapter, activity: NewsFragment,
                                  userSession: UserSession): AdmobBannerRecyclerAdapterWrapper =
            AdmobBannerRecyclerAdapterWrapper.builder(context)
                    .setLimitOfAds(userSession.countAd?.limitOfAdsNews ?: 20)
                    .setFirstAdIndex(userSession.countAd?.FirstAdIndexNews ?: 10)
                    .setAdViewWrappingStrategy(object : BannerAdViewWrappingStrategy() {
                        override fun addAdViewToWrapper(wrapper: ViewGroup, ad: AdView) {
                            val container = wrapper.findViewById(R.id.adsCardView) as ViewGroup
                            container.removeAllViews()
                            //container.addView(ad)
                            val t = Appodeal.getNativeAds(1)
                            if (t.size>0) {
                                val nativeAdView = NativeAdViewAppWall(activity.requireActivity(), t[0], NEWS)
                                container.addView(nativeAdView)
                            }// else {
                                //wrapper.visibility = View.GONE
                            //}
                        }
                        override fun getAdViewWrapper(parent: ViewGroup?): ViewGroup {
                            return LayoutInflater.from(parent?.context).inflate(R.layout.layout_admob_news,
                                    parent, false) as ViewGroup
                        }
                    })
                    .setNoOfDataBetweenAds(userSession.countAd?.noOfDataBetweenAdsNews ?: 7)
                    //.setSingleAdUnitId(BuildConfig.BANNER_AD_UNIT_ID)
                    //.setTestDeviceIds(arrayOf("BA4CB07CBCAA1F64824EE76EC089BA5A"))
                    .setAdapter(newsAdapter)
                    .build()

    @PerFragment
    @Provides
    fun provideLinearLayoutManager(fragment: NewsFragment): RecyclerView.LayoutManager =
            LinearLayoutManager(fragment.context, LinearLayoutManager.VERTICAL, false)

}
