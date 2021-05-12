package com.intergroupapplication.presentation.feature.commentsbottomsheet.di

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
import com.intergroupapplication.presentation.feature.commentsbottomsheet.adapter.*
import com.intergroupapplication.presentation.feature.commentsbottomsheet.view.CommentBottomSheetFragment
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.manager.DialogProvider
import com.intergroupapplication.presentation.manager.ToastManager
import com.mobsandgeeks.saripaar.Validator
import com.yalantis.ucrop.UCrop
import dagger.Module
import dagger.Provides

@Module()
class BottomSheetViewModule {

    @PerFragment
    @Provides
    fun provideGalleryAdapter(imageLoadingDelegate: ImageLoadingDelegate
                              , callback: CommentBottomSheetFragment,
                              dialogDelegate: DialogDelegate): GalleryAdapter {
        return GalleryAdapter(imageLoadingDelegate,callback,dialogDelegate,
                callback.childFragmentManager)
    }

    @PerFragment
    @Provides
    fun provideAudioAdapter(callback: CommentBottomSheetFragment): AudioAdapter{
        return AudioAdapter(callback)
    }

    @PerFragment
    @Provides
    fun provideVideoAdapter(imageLoadingDelegate: ImageLoadingDelegate,
                            callback: CommentBottomSheetFragment,
                            dialogDelegate: DialogDelegate): VideoAdapter {
        return VideoAdapter(imageLoadingDelegate,callback,dialogDelegate,
                callback.childFragmentManager)
    }

    @PerFragment
    @Provides
    fun providePlaylistAdapter(imageLoadingDelegate: ImageLoadingDelegate): PlaylistAdapter {
        return PlaylistAdapter(imageLoadingDelegate)
    }

    @PerFragment
    @Provides
    fun provideValidator(fragmentComment: CommentBottomSheetFragment): Validator =
            Validator(fragmentComment).apply { setValidationListener(fragmentComment) }

    @PerFragment
    @Provides
    fun providePhotoGateway(fragmentComment: CommentBottomSheetFragment,
                            cropOptions: UCrop.Options,
                            api: AppApi, awsUploadingGateway: AwsUploadingGateway): PhotoGateway =
            PhotoRepository(fragmentComment.requireActivity(), cropOptions, api, awsUploadingGateway)

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
    fun provideDialogManager(fragmentComment: CommentBottomSheetFragment): DialogManager =
            DialogManager(fragmentComment.requireActivity().supportFragmentManager)

    @PerFragment
    @Provides
    fun dialogDelegate(dialogManager: DialogManager, dialogProvider: DialogProvider, toastManager: ToastManager,
                       context: Context)
            : DialogDelegate =
            DialogDelegate(dialogManager, dialogProvider, toastManager, context)

}