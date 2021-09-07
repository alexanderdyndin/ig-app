package com.intergroupapplication.presentation.feature.group.di

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
import com.intergroupapplication.presentation.base.ImageUploader
import com.intergroupapplication.presentation.base.adapter.PagingLoadingAdapter
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.delegate.ImageUploadingDelegate
import com.intergroupapplication.presentation.feature.group.adapter.GroupPostsAdapter
import com.intergroupapplication.presentation.feature.group.view.GroupFragment
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.provider.DialogProvider
import com.yalantis.ucrop.UCrop
import dagger.Module
import dagger.Provides


@Module
class GroupViewModule {

    companion object {
        const val COMMENT_POST_ENTITY = "comment_post"
    }

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
    fun providePhotoGateway(
        fragment: GroupFragment, cropOptions: UCrop.Options,
        api: AppApi, awsUploadingGateway: AwsUploadingGateway
    ): PhotoGateway =
        PhotoRepository(fragment, cropOptions, api, awsUploadingGateway)

    @PerFragment
    @Provides
    fun provideImageUploader(photoGateway: PhotoGateway): ImageUploader =
        ImageUploadingDelegate(photoGateway)


    @PerFragment
    @Provides
    fun provideDialogManager(fragment: GroupFragment): DialogManager =
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
    fun provideGroupPostsAdapter(
        imageLoadingDelegate: ImageLoadingDelegate,
        userSession: UserSession, proxyCacheServer: HttpProxyCacheServer
    ): GroupPostsAdapter {
        if (userSession.isAdEnabled) {
            GroupPostsAdapter.AD_FREQ = userSession.countAd?.noOfDataBetweenAdsNews ?: 7
            GroupPostsAdapter.AD_FIRST = userSession.countAd?.firstAdIndexNews ?: 3
            GroupPostsAdapter.AD_TYPE = userSession.countAd?.limitOfAdsNews ?: 1
        } else {
            GroupPostsAdapter.AD_FREQ = 999
            GroupPostsAdapter.AD_FIRST = 999
        }
        return GroupPostsAdapter(imageLoadingDelegate, proxyCacheServer)
    }

    @PerFragment
    @Provides
    @Footer
    fun provideFooterAdapter(groupPostsAdapter: GroupPostsAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { groupPostsAdapter.retry() }
    }

    @PerFragment
    @Provides
    @Header
    fun provideHeaderAdapter(groupPostsAdapter: GroupPostsAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { groupPostsAdapter.retry() }
    }

    @PerFragment
    @Provides
    fun provideConcatAdapter(
        groupPostsAdapter: GroupPostsAdapter,
        @Footer footerAdapter: PagingLoadingAdapter,
        @Header headerAdapter: PagingLoadingAdapter
    ): ConcatAdapter {
        return groupPostsAdapter.withLoadStateHeaderAndFooter(headerAdapter, footerAdapter)
    }
}
