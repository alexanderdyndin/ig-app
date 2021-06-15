package com.intergroupapplication.presentation.feature.videolist.di

import android.content.Context
import androidx.recyclerview.widget.ConcatAdapter
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.presentation.base.adapter.PagingLoadingAdapter
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.feature.videolist.adapter.VideoListAdapter
import com.intergroupapplication.presentation.feature.videolist.view.VideoListFragment
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.manager.DialogProvider
import com.intergroupapplication.presentation.manager.ToastManager
import dagger.Module
import dagger.Provides
import javax.inject.Named


@Module
class VideoListViewModule {
    @PerFragment
    @Provides
    fun provideDialogManager(fragment: VideoListFragment): DialogManager =
        DialogManager(fragment.requireActivity().supportFragmentManager)


    @PerFragment
    @Provides
    fun dialogDelegate(dialogManager: DialogManager, dialogProvider: DialogProvider, toastManager: ToastManager,
                       context: Context
    )
            : DialogDelegate =
        DialogDelegate(dialogManager, dialogProvider, toastManager, context)

    @PerFragment
    @Provides
    @Named("footer")
    fun provideFooterAdapter(@Named("all") videoListAdapter: VideoListAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { videoListAdapter.retry() }
    }

    @PerFragment
    @Provides
    @Named("header")
    fun provideHeaderAdapter(@Named("all") videoListAdapter: VideoListAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { videoListAdapter.retry() }
    }

    @PerFragment
    @Provides
    @Named("all")
    fun provideAllMusic(): VideoListAdapter {
        return VideoListAdapter()
    }

    @PerFragment
    @Provides
    @Named("all")
    fun provideConcatListAll(@Named("all") videoListAdapter: VideoListAdapter,
                             @Named("footer") pagingFooter: PagingLoadingAdapter,
                             @Named("header") pagingHeader: PagingLoadingAdapter,
    ): ConcatAdapter {
        return videoListAdapter.withLoadStateHeaderAndFooter(pagingHeader, pagingFooter)
    }
}