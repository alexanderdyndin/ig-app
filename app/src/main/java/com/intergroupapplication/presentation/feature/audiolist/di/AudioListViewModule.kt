package com.intergroupapplication.presentation.feature.audiolist.di

import android.content.Context
import androidx.recyclerview.widget.ConcatAdapter
import com.intergroupapplication.di.qualifier.All
import com.intergroupapplication.di.qualifier.Footer
import com.intergroupapplication.di.qualifier.Header
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.presentation.base.adapter.PagingLoadingAdapter
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.feature.audiolist.adapter.AudioListAdapter
import com.intergroupapplication.presentation.feature.audiolist.view.AudioListFragment
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.provider.DialogProvider
import dagger.Module
import dagger.Provides

@Module
class AudioListViewModule {
    @PerFragment
    @Provides
    fun provideDialogManager(fragment: AudioListFragment): DialogManager =
        DialogManager(fragment.requireActivity().supportFragmentManager)


    @PerFragment
    @Provides
    fun dialogDelegate(
        dialogManager: DialogManager, dialogProvider: DialogProvider,
        context: Context
    )
            : DialogDelegate =
        DialogDelegate(dialogManager, dialogProvider, context)

    @PerFragment
    @Provides
    @Footer
    fun provideFooterAdapter(@All audioListAdapter: AudioListAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { audioListAdapter.retry() }
    }

    @PerFragment
    @Provides
    @Header
    fun provideHeaderAdapter(@All audioListAdapter: AudioListAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { audioListAdapter.retry() }
    }

    @PerFragment
    @Provides
    @All
    fun provideAllMusic(): AudioListAdapter {
        return AudioListAdapter()
    }

    @PerFragment
    @Provides
    @All
    fun provideConcatListAll(
        @All audioListAdapter: AudioListAdapter,
        @Footer pagingFooter: PagingLoadingAdapter,
        @Header pagingHeader: PagingLoadingAdapter,
    ): ConcatAdapter {
        return audioListAdapter.withLoadStateHeaderAndFooter(pagingHeader, pagingFooter)
    }
}
