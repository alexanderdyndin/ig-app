package com.intergroupapplication.presentation.feature.bottomsheet.di

import android.content.Context
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.repository.PhotoRepository
import com.intergroupapplication.di.module.AppBuilderModule
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.domain.gateway.AwsUploadingGateway
import com.intergroupapplication.domain.gateway.PhotoGateway
import com.intergroupapplication.presentation.base.FrescoImageLoader
import com.intergroupapplication.presentation.base.ImageLoader
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.feature.bottomsheet.adapter.*
import com.intergroupapplication.presentation.feature.bottomsheet.presenter.BottomSheetPresenter
import com.intergroupapplication.presentation.feature.bottomsheet.view.BottomSheetFragment
import com.intergroupapplication.presentation.feature.commentsdetails.view.CommentsDetailsFragment
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
    fun provideGalleryAdapter(imageLoadingDelegate: ImageLoadingDelegate,
    presenter: BottomSheetPresenter,callback: BottomSheetFragment): GalleryAdapter {
        return GalleryAdapter(imageLoadingDelegate,presenter,callback)
    }

    @PerFragment
    @Provides
    fun provideAudioAdapter(callback: BottomSheetFragment): AudioAdapter{
        return AudioAdapter(callback)
    }

    @PerFragment
    @Provides
    fun provideVideoAdapter(imageLoadingDelegate: ImageLoadingDelegate,
                            callback: BottomSheetFragment): VideoAdapter {
        return VideoAdapter(imageLoadingDelegate,callback)
    }

    @PerFragment
    @Provides
    fun providePlaylistAdapter(imageLoadingDelegate: ImageLoadingDelegate): PlaylistAdapter {
        return PlaylistAdapter(imageLoadingDelegate)
    }

    @PerFragment
    @Provides
    fun provideValidator(fragment: BottomSheetFragment): Validator =
            Validator(fragment).apply { setValidationListener(fragment) }

    @PerFragment
    @Provides
    fun providePhotoGateway(fragment: BottomSheetFragment,
                            cropOptions: UCrop.Options,
                            api: AppApi, awsUploadingGateway: AwsUploadingGateway): PhotoGateway =
            PhotoRepository(fragment.requireActivity(), cropOptions, api, awsUploadingGateway)


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
    fun provideDialogManager(fragment: BottomSheetFragment): DialogManager =
            DialogManager(fragment.requireActivity().supportFragmentManager)

    @PerFragment
    @Provides
    fun dialogDelegate(dialogManager: DialogManager, dialogProvider: DialogProvider, toastManager: ToastManager,
                       context: Context)
            : DialogDelegate =
            DialogDelegate(dialogManager, dialogProvider, toastManager, context)

}