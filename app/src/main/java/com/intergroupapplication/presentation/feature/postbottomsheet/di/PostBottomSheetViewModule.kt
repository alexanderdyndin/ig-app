package com.intergroupapplication.presentation.feature.postbottomsheet.di

import android.content.Context
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.repository.PhotoRepository
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.domain.gateway.AwsUploadingGateway
import com.intergroupapplication.domain.gateway.PhotoGateway
import com.intergroupapplication.presentation.base.FrescoImageLoader
import com.intergroupapplication.presentation.base.ImageLoader
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.feature.commentsbottomsheet.adapter.MediaAdapter
import com.intergroupapplication.presentation.feature.postbottomsheet.view.PostBottomSheetFragment
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.manager.ToastManager
import com.intergroupapplication.presentation.provider.DialogProvider
import com.yalantis.ucrop.UCrop
import dagger.Module
import dagger.Provides

@Module
class PostBottomSheetViewModule {
    @PerFragment
    @Provides
    fun provideGalleryAdapter(
        imageLoadingDelegate: ImageLoadingDelegate, callback: PostBottomSheetFragment,
        dialogDelegate: DialogDelegate
    ): MediaAdapter.GalleryAdapter {
        return MediaAdapter.GalleryAdapter(imageLoadingDelegate, callback, dialogDelegate)
    }

    @PerFragment
    @Provides
    fun provideAudioAdapter(callback: PostBottomSheetFragment): MediaAdapter.AudioAdapter {
        return MediaAdapter.AudioAdapter(callback)
    }

    @PerFragment
    @Provides
    fun provideVideoAdapter(
        imageLoadingDelegate: ImageLoadingDelegate,
        callback: PostBottomSheetFragment,
        dialogDelegate: DialogDelegate
    ): MediaAdapter.VideoAdapter {
        return MediaAdapter.VideoAdapter(imageLoadingDelegate, callback, dialogDelegate)
    }

    @PerFragment
    @Provides
    fun providePlaylistAdapter(imageLoadingDelegate: ImageLoadingDelegate):
            MediaAdapter.PlaylistAdapter {
        return MediaAdapter.PlaylistAdapter(imageLoadingDelegate)
    }

    @PerFragment
    @Provides
    fun provideColorAdapter(callback: PostBottomSheetFragment): MediaAdapter.ColorAdapter {
        return MediaAdapter.ColorAdapter(callback)
    }

    @PerFragment
    @Provides
    fun providePhotoGateway(
        fragment: PostBottomSheetFragment,
        cropOptions: UCrop.Options,
        api: AppApi, awsUploadingGateway: AwsUploadingGateway
    ): PhotoGateway =
        PhotoRepository(fragment, cropOptions, api, awsUploadingGateway)

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
    fun provideDialogManager(fragmentComment: PostBottomSheetFragment): DialogManager =
        DialogManager(fragmentComment.childFragmentManager)

    @PerFragment
    @Provides
    fun dialogDelegate(
        dialogManager: DialogManager, dialogProvider: DialogProvider, toastManager: ToastManager,
        context: Context
    ): DialogDelegate =
        DialogDelegate(dialogManager, dialogProvider, toastManager, context)
}