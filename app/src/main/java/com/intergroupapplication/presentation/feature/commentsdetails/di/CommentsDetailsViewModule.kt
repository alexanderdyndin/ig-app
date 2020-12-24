package com.intergroupapplication.presentation.feature.commentsdetails.di

import android.content.Context
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.repository.PhotoRepository
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.domain.entity.CommentEntity
import com.intergroupapplication.domain.gateway.AwsUploadingGateway
import com.intergroupapplication.domain.gateway.PhotoGateway
import com.intergroupapplication.presentation.base.FrescoImageLoader
import com.intergroupapplication.presentation.base.ImageLoader
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.feature.commentsdetails.adapter.CommentDetailsAdapter
import com.intergroupapplication.presentation.feature.commentsdetails.view.CommentsDetailsFragment
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.manager.DialogProvider
import com.intergroupapplication.presentation.manager.ToastManager
import com.mobsandgeeks.saripaar.Validator
import com.yalantis.ucrop.UCrop
import dagger.Module
import dagger.Provides


@Module
class CommentsDetailsViewModule {

    @PerFragment
    @Provides
    fun provideGroupPostEntityDiffUtilCallback() = object : DiffUtil.ItemCallback<CommentEntity>() {
        override fun areItemsTheSame(oldItem: CommentEntity, newItem: CommentEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: CommentEntity, newItem: CommentEntity) = oldItem == newItem
    }

    @PerFragment
    @Provides
    fun provideCommentDetailsAdapter(diffUtil: DiffUtil.ItemCallback<CommentEntity>,
                                     imageLoadingDelegate: ImageLoadingDelegate): CommentDetailsAdapter =
            CommentDetailsAdapter(diffUtil, imageLoadingDelegate)

    @PerFragment
    @Provides
    fun provideValidator(fragment: CommentsDetailsFragment): Validator =
            Validator(fragment).apply { setValidationListener(fragment) }

    @PerFragment
    @Provides
    fun providePhotoGateway(fragment: CommentsDetailsFragment, cropOptions: UCrop.Options,
                            api: AppApi, awsUploadingGateway: AwsUploadingGateway): PhotoGateway =
            PhotoRepository(fragment.requireActivity(), cropOptions, api, awsUploadingGateway)


    @PerFragment
    @Provides
    fun provideFrescoImageLoader(fragment: CommentsDetailsFragment): ImageLoader =
            FrescoImageLoader(fragment.requireActivity())


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
    fun dialogDelegate(dialogManager: DialogManager, dialogProvider: DialogProvider, toastManager: ToastManager,
                       context: Context)
            : DialogDelegate =
            DialogDelegate(dialogManager, dialogProvider, toastManager, context)


    @PerFragment
    @Provides
    fun provideLinearLayoutManager(fragment: CommentsDetailsFragment): RecyclerView.LayoutManager =
            LinearLayoutManager(fragment.requireActivity(), LinearLayoutManager.VERTICAL, false)

}
