package com.intergroupapplication.presentation.feature.news.di

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
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.delegate.ImageUploadingDelegate
import com.intergroupapplication.presentation.base.adapter.PagingLoadingAdapter
import com.intergroupapplication.presentation.feature.news.adapter.NewsAdapter
import com.intergroupapplication.presentation.feature.news.view.NewsFragment
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.manager.DialogProvider
import com.intergroupapplication.presentation.manager.ToastManager
import com.yalantis.ucrop.UCrop
import dagger.Module
import dagger.Provides
import javax.inject.Named


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

//    @PerFragment
//    @Provides
//    fun provideGroupPostEntityDiffUtilCallback() = object : DiffUtil.ItemCallback<GroupPostEntity>() {
//        override fun areItemsTheSame(oldItem: GroupPostEntity, newItem: GroupPostEntity) = oldItem.id == newItem.id
//        override fun areContentsTheSame(oldItem: GroupPostEntity, newItem: GroupPostEntity) = oldItem == newItem
//    }

    @PerFragment
    @Provides
    fun provideNewsAdapter(imageLoadingDelegate: ImageLoadingDelegate,
                           userSession: UserSession): NewsAdapter {
        NewsAdapter.AD_FREQ = userSession.countAd?.noOfDataBetweenAdsNews ?: 7
        NewsAdapter.AD_FIRST = userSession.countAd?.firstAdIndexNews ?: 3
        return NewsAdapter(imageLoadingDelegate)
    }

    @PerFragment
    @Provides
    @Named("footer")
    fun provideFooterAdapter(newsAdapter: NewsAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { newsAdapter.retry() }
    }

    @PerFragment
    @Provides
    @Named("header")
    fun provideHeaderAdapter(newsAdapter: NewsAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { newsAdapter.retry() }
    }

    @PerFragment
    @Provides
    fun provideConcatAdapter(newsAdapter: NewsAdapter,
                             @Named("footer") footerAdapter: PagingLoadingAdapter,
                             @Named("header") headerAdapter: PagingLoadingAdapter
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
