package com.intergroupapplication.presentation.feature.audiolist.di

import android.content.Context
import androidx.recyclerview.widget.ConcatAdapter
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.presentation.base.adapter.PagingLoadingAdapter
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.feature.audiolist.adapter.AudioListAdapter
import com.intergroupapplication.presentation.feature.audiolist.view.AudioListFragment
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.provider.DialogProvider
import com.intergroupapplication.presentation.manager.ToastManager
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class AudioListViewModule {
    @PerFragment
    @Provides
    fun provideDialogManager(fragment: AudioListFragment): DialogManager =
            DialogManager(fragment.requireActivity().supportFragmentManager)


    @PerFragment
    @Provides
    fun dialogDelegate(dialogManager: DialogManager, dialogProvider: DialogProvider, toastManager: ToastManager,
                       context: Context)
            : DialogDelegate =
            DialogDelegate(dialogManager, dialogProvider, toastManager, context)

    @PerFragment
    @Provides
    @Named("footer")
    fun provideFooterAdapter(@Named("all") audioListAdapter: AudioListAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { audioListAdapter.retry() }
    }

    @PerFragment
    @Provides
    @Named("header")
    fun provideHeaderAdapter(@Named("all") audioListAdapter: AudioListAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { audioListAdapter.retry() }
    }

    @PerFragment
    @Provides
    @Named("all")
    fun provideAllMusic(): AudioListAdapter {
        return AudioListAdapter()
    }

    @PerFragment
    @Provides
    @Named("all")
    fun provideConcatListAll(@Named("all") audioListAdapter: AudioListAdapter,
                             @Named("footer") pagingFooter: PagingLoadingAdapter,
                             @Named("header") pagingHeader: PagingLoadingAdapter,
    ): ConcatAdapter {
        return audioListAdapter.withLoadStateHeaderAndFooter(pagingHeader, pagingFooter)
    }
}