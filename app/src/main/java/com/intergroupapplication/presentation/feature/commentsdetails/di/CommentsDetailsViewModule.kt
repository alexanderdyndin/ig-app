package com.intergroupapplication.presentation.feature.commentsdetails.di

import android.content.Context
import androidx.recyclerview.widget.ConcatAdapter
import com.danikula.videocache.HttpProxyCacheServer
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.repository.PhotoRepository
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.di.qualifier.Footer
import com.intergroupapplication.di.qualifier.Header
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.domain.gateway.AwsUploadingGateway
import com.intergroupapplication.domain.gateway.PhotoGateway
import com.intergroupapplication.presentation.base.FrescoImageLoader
import com.intergroupapplication.presentation.base.ImageLoader
import com.intergroupapplication.presentation.base.adapter.PagingLoadingAdapter
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.feature.commentsdetails.adapter.CommentsAdapter
import com.intergroupapplication.presentation.feature.commentsdetails.view.CommentsDetailsFragment
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.provider.DialogProvider
import com.yalantis.ucrop.UCrop
import dagger.Module
import dagger.Provides

@Module
class CommentsDetailsViewModule {

    @PerFragment
    @Provides
    fun providePhotoGateway(
        fragment: CommentsDetailsFragment, cropOptions: UCrop.Options,
        api: AppApi, awsUploadingGateway: AwsUploadingGateway
    ): PhotoGateway =
        PhotoRepository(fragment, cropOptions, api, awsUploadingGateway)


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
    fun provideDialogManager(fragment: CommentsDetailsFragment): DialogManager =
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
    fun provideCommentsAdapter(
        imageLoadingDelegate: ImageLoadingDelegate,
        userSession: UserSession,
        proxyCacheServer: HttpProxyCacheServer,
        fragment: CommentsDetailsFragment
    ): CommentsAdapter {
        if (userSession.isAdEnabled) {
            CommentsAdapter.AD_TYPE = userSession.countAd?.limitOfAdsComments ?: 1
            CommentsAdapter.AD_FREQ = userSession.countAd?.noOfDataBetweenAdsComments ?: 7
            CommentsAdapter.AD_FIRST = userSession.countAd?.firstAdIndexComments ?: 3
        } else {
            CommentsAdapter.AD_FREQ = 999
            CommentsAdapter.AD_FIRST = 999
        }
        return CommentsAdapter(
            imageLoadingDelegate,
            proxyCacheServer,
            fragment.childFragmentManager
        )
    }

    @PerFragment
    @Provides
    @Footer
    fun provideFooterAdapter(commentsAdapter: CommentsAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { commentsAdapter.retry() }
    }

    @PerFragment
    @Provides
    @Header
    fun provideHeaderAdapter(commentsAdapter: CommentsAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { commentsAdapter.retry() }
    }

    @PerFragment
    @Provides
    fun provideConcatAdapter(
        commentsAdapter: CommentsAdapter,
        @Footer footerAdapter: PagingLoadingAdapter,
        @Header headerAdapter: PagingLoadingAdapter
    ): ConcatAdapter {
        return commentsAdapter.withLoadStateHeaderAndFooter(headerAdapter, footerAdapter)
    }
}
