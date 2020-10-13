package com.intergroupapplication.presentation.feature.commentsdetails.di

import android.content.Context
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.repository.PhotoRepository
import com.intergroupapplication.di.scope.PerActivity
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.domain.entity.CommentEntity
import com.intergroupapplication.domain.gateway.AwsUploadingGateway
import com.intergroupapplication.domain.gateway.PhotoGateway
import com.intergroupapplication.presentation.base.FrescoImageLoader
import com.intergroupapplication.presentation.base.ImageLoader
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.feature.commentsdetails.adapter.CommentDetailsAdapter
import com.intergroupapplication.presentation.feature.commentsdetails.view.CommentsDetailsActivity
import com.intergroupapplication.presentation.feature.news.view.NewsFragment
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.manager.DialogProvider
import com.intergroupapplication.presentation.manager.ToastManager
import com.mobsandgeeks.saripaar.Validator
import com.yalantis.ucrop.UCrop
import dagger.Module
import dagger.Provides
import ru.terrakok.cicerone.android.support.SupportAppNavigator

@Module
class CommentsDetailsViewModule {

    @PerActivity
    @Provides
    fun provideGroupPostEntityDiffUtilCallback() = object : DiffUtil.ItemCallback<CommentEntity>() {
        override fun areItemsTheSame(oldItem: CommentEntity, newItem: CommentEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: CommentEntity, newItem: CommentEntity) = oldItem == newItem
    }

    @PerActivity
    @Provides
    fun provideCommentDetailsAdapter(diffUtil: DiffUtil.ItemCallback<CommentEntity>,
                                     imageLoadingDelegate: ImageLoadingDelegate): CommentDetailsAdapter =
            CommentDetailsAdapter(diffUtil, imageLoadingDelegate)

    @PerActivity
    @Provides
    fun provideValidator(activity: CommentsDetailsActivity): Validator =
            Validator(activity).apply { setValidationListener(activity) }

    @PerActivity
    @Provides
    fun providePhotoGateway(activity: CommentsDetailsActivity, cropOptions: UCrop.Options,
                            api: AppApi, awsUploadingGateway: AwsUploadingGateway): PhotoGateway =
            PhotoRepository(activity, cropOptions, api, awsUploadingGateway)


    @PerActivity
    @Provides
    fun provideFrescoImageLoader(activity: CommentsDetailsActivity): ImageLoader =
            FrescoImageLoader(activity)


    @PerActivity
    @Provides
    fun provideImageLoadingDelegate(imageLoader: ImageLoader): ImageLoadingDelegate =
            ImageLoadingDelegate(imageLoader)


    @PerActivity
    @Provides
    fun provideDialogManager(activity: CommentsDetailsActivity): DialogManager =
            DialogManager(activity.supportFragmentManager)


    @PerActivity
    @Provides
    fun dialogDelegate(dialogManager: DialogManager, dialogProvider: DialogProvider, toastManager: ToastManager,
                       context: Context)
            : DialogDelegate =
            DialogDelegate(dialogManager, dialogProvider, toastManager, context)


    @PerActivity
    @Provides
    fun provideSupportAppNavigator(activity: CommentsDetailsActivity): SupportAppNavigator =
            SupportAppNavigator(activity, 0)

    @PerActivity
    @Provides
    fun provideLinearLayoutManager(activity: CommentsDetailsActivity): RecyclerView.LayoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

}
