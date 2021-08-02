package com.intergroupapplication.presentation.feature.grouplist.di

import android.content.Context
import androidx.recyclerview.widget.ConcatAdapter
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.repository.PhotoRepository
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.di.qualifier.*
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
import com.intergroupapplication.presentation.feature.grouplist.adapter.GroupListsAdapter
import com.intergroupapplication.presentation.feature.grouplist.view.GroupListFragment
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.manager.ToastManager
import com.intergroupapplication.presentation.provider.DialogProvider
import com.yalantis.ucrop.UCrop
import dagger.Module
import dagger.Provides

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
    fun providePhotoGateway(
        activity: GroupListFragment, cropOptions: UCrop.Options,
        api: AppApi, awsUploadingGateway: AwsUploadingGateway
    ): PhotoGateway =
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
    fun dialogDelegate(
        dialogManager: DialogManager, dialogProvider: DialogProvider, toastManager: ToastManager,
        context: Context
    )
            : DialogDelegate =
        DialogDelegate(dialogManager, dialogProvider, toastManager, context)

    @PerFragment
    @Provides
    @FooterAll
    fun provideFooterAdapterAll(@All groupListAdapter: GroupListAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { groupListAdapter.retry() }
    }

    @PerFragment
    @Provides
    @HeaderAll
    fun provideHeaderAdapterAll(@All groupListAdapter: GroupListAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { groupListAdapter.retry() }
    }

    @PerFragment
    @Provides
    @FooterSub
    fun provideFooterAdapterSub(@Subscribed groupListAdapter: GroupListAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { groupListAdapter.retry() }
    }

    @PerFragment
    @Provides
    @HeaderSub
    fun provideHeaderAdapterSub(@Subscribed groupListAdapter: GroupListAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { groupListAdapter.retry() }
    }

    @PerFragment
    @Provides
    @FooterAdm
    fun provideFooterAdapterAdm(@Owned groupListAdapter: GroupListAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { groupListAdapter.retry() }
    }

    @PerFragment
    @Provides
    @HeaderAdm
    fun provideHeaderAdapterAdm(@Owned groupListAdapter: GroupListAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { groupListAdapter.retry() }
    }

    @PerFragment
    @Provides
    @All
    fun provideGroupListAll(
        imageLoadingDelegate: ImageLoadingDelegate,
        userSession: UserSession
    ): GroupListAdapter {
        if (userSession.isAdEnabled) {
            GroupListAdapter.AD_FIRST = userSession.countAd?.firstAdIndexGroups ?: 5
            GroupListAdapter.AD_FREQ = userSession.countAd?.noOfDataBetweenAdsGroups ?: 5
            GroupListAdapter.AD_TYPE = userSession.countAd?.limitOfAdsGroups ?: 1
        } else {
            GroupListAdapter.AD_FIRST = 999
            GroupListAdapter.AD_FREQ = 999
        }
        return GroupListAdapter(imageLoadingDelegate)
    }


    @PerFragment
    @Provides
    @Subscribed
    fun provideGroupListSub(imageLoadingDelegate: ImageLoadingDelegate): GroupListAdapter {
        return GroupListAdapter(imageLoadingDelegate)
    }

    @PerFragment
    @Provides
    @Owned
    fun provideConcatGroupListAdm(imageLoadingDelegate: ImageLoadingDelegate): GroupListAdapter {
        return GroupListAdapter(imageLoadingDelegate)
    }

    @PerFragment
    @Provides
    @All
    fun provideConcatListAll(
        @All groupListAdapter: GroupListAdapter,
        @FooterAll pagingFooter: PagingLoadingAdapter,
        @HeaderAll pagingHeader: PagingLoadingAdapter,
    ): ConcatAdapter {
        return groupListAdapter.withLoadStateHeaderAndFooter(pagingHeader, pagingFooter)
    }


    @PerFragment
    @Provides
    @Subscribed
    fun provideConcatListSub(
        @Subscribed groupListAdapter: GroupListAdapter,
        @FooterSub pagingFooter: PagingLoadingAdapter,
        @HeaderSub pagingHeader: PagingLoadingAdapter,
    ): ConcatAdapter {
        return groupListAdapter.withLoadStateHeaderAndFooter(pagingHeader, pagingFooter)
    }

    @PerFragment
    @Provides
    @Owned
    fun provideGroupListAdm(
        @Owned groupListAdapter: GroupListAdapter,
        @FooterAdm pagingFooter: PagingLoadingAdapter,
        @HeaderAdm pagingHeader: PagingLoadingAdapter,
    ): ConcatAdapter {
        return groupListAdapter.withLoadStateHeaderAndFooter(pagingHeader, pagingFooter)
    }

    @PerFragment
    @Provides
    fun provideGroupListsAdapter(
        @All adapterAll: ConcatAdapter,
        @Subscribed adapterSub: ConcatAdapter,
        @Owned adapterAdm: ConcatAdapter
    ): GroupListsAdapter = GroupListsAdapter(listOf(adapterAll, adapterSub, adapterAdm))
}
