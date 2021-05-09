package com.intergroupapplication.presentation.feature.userlist.addBlackListById

import android.content.Context
import androidx.recyclerview.widget.ConcatAdapter
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.presentation.base.FrescoImageLoader
import com.intergroupapplication.presentation.base.ImageLoader
import com.intergroupapplication.presentation.base.adapter.PagingLoadingAdapter
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class AddBlackListByIdViewModule {

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
    fun provideAddUserBlackListAdapter(): AddUserBlackListAdapter {
        return AddUserBlackListAdapter()
    }

    @PerFragment
    @Provides
    @Named("headerBanList")
    fun provideHeaderAddUserBlackListAdapter(
            userBlackListAdapter: AddUserBlackListAdapter
    ): PagingLoadingAdapter{
        return PagingLoadingAdapter { userBlackListAdapter.retry() }
    }

    @PerFragment
    @Provides
    @Named("footerBanList")
    fun provideFooterAddUserBlackListAdapter(
            userBlackListAdapter: AddUserBlackListAdapter
    ): PagingLoadingAdapter{
        return PagingLoadingAdapter { userBlackListAdapter.retry() }
    }

    @PerFragment
    @Provides
    @Named("blackListDialog")
    fun provideConcatAddUserBlackListAdapter(
            userBlackListAdapter: AddUserBlackListAdapter,
            @Named("headerBanList") pagingHeader: PagingLoadingAdapter,
            @Named("footerBanList") pagingFooter: PagingLoadingAdapter
    ): ConcatAdapter {
        return userBlackListAdapter.withLoadStateHeaderAndFooter(pagingHeader, pagingFooter)
    }
}