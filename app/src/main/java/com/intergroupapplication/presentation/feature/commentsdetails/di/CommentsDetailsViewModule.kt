package com.intergroupapplication.presentation.feature.commentsdetails.di

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
import com.intergroupapplication.presentation.base.adapter.PagingLoadingAdapter
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.feature.commentsdetails.adapter.CommentsAdapter
import com.intergroupapplication.presentation.feature.commentsdetails.view.CommentsDetailsFragment
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.manager.DialogProvider
import com.intergroupapplication.presentation.manager.ToastManager
import com.mobsandgeeks.saripaar.Validator
import com.yalantis.ucrop.UCrop
import dagger.Module
import dagger.Provides
import javax.inject.Named


@Module
class CommentsDetailsViewModule {

//    @PerFragment
//    @Provides
//    fun provideGroupPostEntityDiffUtilCallback() = object : DiffUtil.ItemCallback<CommentEntity>() {
//        override fun areItemsTheSame(oldItem: CommentEntity, newItem: CommentEntity) = oldItem.id == newItem.id
//        override fun areContentsTheSame(oldItem: CommentEntity, newItem: CommentEntity) = oldItem == newItem
//    }

//    @PerFragment
//    @Provides
//    fun provideCommentDetailsAdapter(diffUtil: DiffUtil.ItemCallback<CommentEntity>,
//                                     imageLoadingDelegate: ImageLoadingDelegate): CommentDetailsAdapter =
//            CommentDetailsAdapter(diffUtil, imageLoadingDelegate)

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
    fun provideFrescoImageLoader(context: Context): ImageLoader =
            FrescoImageLoader(context)


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


//    @PerFragment
//    @Provides
//    fun provideLinearLayoutManager(fragment: CommentsDetailsFragment): RecyclerView.LayoutManager =
//            LinearLayoutManager(fragment.requireActivity(), LinearLayoutManager.VERTICAL, false)

    @PerFragment
    @Provides
    fun provideCommentsAdapter(imageLoadingDelegate: ImageLoadingDelegate,
                           userSession: UserSession): CommentsAdapter {
        if (userSession.isAdEnabled) {
            CommentsAdapter.AD_TYPE = 1 //userSession.countAd?.limitOfAdsComments ?: 1
            CommentsAdapter.AD_FREQ = userSession.countAd?.noOfDataBetweenAdsComments ?: 7
            CommentsAdapter.AD_FIRST = userSession.countAd?.firstAdIndexComments ?: 3
        } else {
            CommentsAdapter.AD_FREQ = 999
            CommentsAdapter.AD_FIRST = 999
        }
        return CommentsAdapter(imageLoadingDelegate)
    }

    @PerFragment
    @Provides
    @Named("footer")
    fun provideFooterAdapter(commentsAdapter: CommentsAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { commentsAdapter.retry() }
    }

    @PerFragment
    @Provides
    @Named("header")
    fun provideHeaderAdapter(commentsAdapter: CommentsAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { commentsAdapter.retry() }
    }

    @PerFragment
    @Provides
    fun provideConcatAdapter(commentsAdapter: CommentsAdapter,
                             @Named("footer") footerAdapter: PagingLoadingAdapter,
                             @Named("header") headerAdapter: PagingLoadingAdapter
    ): ConcatAdapter {
        return commentsAdapter.withLoadStateHeaderAndFooter(headerAdapter, footerAdapter)
    }

}
