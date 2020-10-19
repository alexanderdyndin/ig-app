package com.intergroupapplication.presentation.feature.group.di

import android.content.Context
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.repository.PhotoRepository
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
import com.intergroupapplication.presentation.feature.group.adapter.GroupAdapter
import com.intergroupapplication.presentation.feature.group.view.GroupActivity
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.manager.DialogProvider
import com.intergroupapplication.presentation.manager.ToastManager
import com.yalantis.ucrop.UCrop
import dagger.Module
import dagger.Provides
import ru.terrakok.cicerone.android.support.SupportAppNavigator

@Module
class GroupViewModule {

    companion object {
        const val COMMENT_POST_ENTITY = "comment_post"
    }

    @PerActivity
    @Provides
    fun provideFrescoImageLoader(activity: GroupActivity): ImageLoader =
            FrescoImageLoader(activity)

    @PerActivity
    @Provides
    fun provideImageLoadingDelegate(imageLoader: ImageLoader): ImageLoadingDelegate =
            ImageLoadingDelegate(imageLoader)

    @PerActivity
    @Provides
    fun providePhotoGateway(activity: GroupActivity, cropOptions: UCrop.Options,
                            api: AppApi, awsUploadingGateway: AwsUploadingGateway): PhotoGateway =
            PhotoRepository(activity, cropOptions, api, awsUploadingGateway)

    @PerActivity
    @Provides
    fun provideImageUploader(photoGateway: PhotoGateway): ImageUploader =
            ImageUploadingDelegate(photoGateway)


    @PerActivity
    @Provides
    fun provideDialogManager(activity: GroupActivity): DialogManager =
            DialogManager(activity.supportFragmentManager)

    @PerActivity
    @Provides
    fun dialogDelegate(dialogManager: DialogManager, dialogProvider: DialogProvider, toastManager: ToastManager,
                       context: Context)
            : DialogDelegate =
            DialogDelegate(dialogManager, dialogProvider, toastManager, context)

    @PerActivity
    @Provides
    fun provideGroupPostEntityDiffUtilCallback() = object : DiffUtil.ItemCallback<GroupPostEntity>() {
        override fun areItemsTheSame(oldItem: GroupPostEntity, newItem: GroupPostEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: GroupPostEntity, newItem: GroupPostEntity) = oldItem == newItem
    }

    @PerActivity
    @Provides
    fun provideGroupAdapter(diffUtil: DiffUtil.ItemCallback<GroupPostEntity>,
                            imageLoadingDelegate: ImageLoadingDelegate): GroupAdapter =
            GroupAdapter(diffUtil, imageLoadingDelegate)


    @PerActivity
    @Provides
    fun provideSupportAppNavigator(activity: GroupActivity): SupportAppNavigator =
            SupportAppNavigator(activity, 0)

    @PerActivity
    @Provides
    fun provideLinearLayoutManager(activity: GroupActivity): RecyclerView.LayoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

}