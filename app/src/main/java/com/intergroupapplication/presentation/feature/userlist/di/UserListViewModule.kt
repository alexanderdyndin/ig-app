package com.intergroupapplication.presentation.feature.userlist.di

import android.content.Context
import androidx.recyclerview.widget.ConcatAdapter
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.presentation.base.FrescoImageLoader
import com.intergroupapplication.presentation.base.ImageLoader
import com.intergroupapplication.presentation.base.adapter.PagingLoadingAdapter
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.feature.userlist.adapter.TypeUserList
import com.intergroupapplication.presentation.feature.userlist.adapter.UserListAdapter
import com.intergroupapplication.presentation.feature.userlist.addBlackListById.AddUserBlackListAdapter
import com.intergroupapplication.presentation.feature.userlist.view.UserListFragment
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.provider.DialogProvider
import com.intergroupapplication.presentation.manager.ToastManager
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class UserListViewModule {
    @PerFragment
    @Provides
    fun provideDialogManager(fragment: UserListFragment): DialogManager =
            DialogManager(fragment.requireActivity().supportFragmentManager)


    @PerFragment
    @Provides
    fun dialogDelegate(dialogManager: DialogManager, dialogProvider: DialogProvider, toastManager: ToastManager,
                       context: Context)
            : DialogDelegate =
            DialogDelegate(dialogManager, dialogProvider, toastManager, context)

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
    @Named("all")
    fun provideUserListAdapterAll(imageLoadingDelegate: ImageLoadingDelegate): UserListAdapter {
        return UserListAdapter(imageLoadingDelegate, TypeUserList.ALL)
    }

    @PerFragment
    @Provides
    @Named("blocked")
    fun provideUserListAdapterBlocked(imageLoadingDelegate: ImageLoadingDelegate): UserListAdapter {
        return UserListAdapter(imageLoadingDelegate, TypeUserList.BLOCKED)
    }

    @PerFragment
    @Provides
    @Named("administrators")
    fun provideUserListAdapterAdministrators(imageLoadingDelegate: ImageLoadingDelegate): UserListAdapter {
        return UserListAdapter(imageLoadingDelegate, TypeUserList.ADMINISTRATORS)
    }

    @PerFragment
    @Provides
    @Named("headerAll")
    fun provideHeaderAdapterAll(@Named("all") userListAdapter: UserListAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { userListAdapter.retry() }
    }

    @PerFragment
    @Provides
    @Named("footerAll")
    fun provideFooterAdapterAll(@Named("all") userListAdapter: UserListAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { userListAdapter.retry() }
    }

    @PerFragment
    @Provides
    @Named("headerBlocked")
    fun provideHeaderAdapterBlocked(@Named("blocked") userListAdapter: UserListAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { userListAdapter.retry() }
    }

    @PerFragment
    @Provides
    @Named("footerBlocked")
    fun provideFooterAdapterBlocked(@Named("blocked") userListAdapter: UserListAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { userListAdapter.retry() }
    }

    @PerFragment
    @Provides
    @Named("headerAdministrators")
    fun provideHeaderAdapterAdministrators(@Named("administrators") userListAdapter: UserListAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { userListAdapter.retry() }
    }

    @PerFragment
    @Provides
    @Named("footerAdministrators")
    fun provideFooterAdapterAdministrators(@Named("administrators") userListAdapter: UserListAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { userListAdapter.retry() }
    }

    @PerFragment
    @Provides
    @Named("all")
    fun provideConcatListAll(
            @Named("all") userListAdapter: UserListAdapter,
            @Named("headerAll") pagingHeader: PagingLoadingAdapter,
            @Named("footerAll") pagingFooter: PagingLoadingAdapter
    ): ConcatAdapter {
        return userListAdapter.withLoadStateHeaderAndFooter(pagingHeader, pagingFooter)
    }

    @PerFragment
    @Provides
    @Named("blocked")
    fun provideConcatListBlocked(
            @Named("blocked") userListAdapter: UserListAdapter,
            @Named("headerBlocked") pagingHeader: PagingLoadingAdapter,
            @Named("footerBlocked") pagingFooter: PagingLoadingAdapter
    ): ConcatAdapter {
        return userListAdapter.withLoadStateHeaderAndFooter(pagingHeader, pagingFooter)
    }

    @PerFragment
    @Provides
    @Named("administrators")
    fun provideConcatListAdministrators(
            @Named("administrators") userListAdapter: UserListAdapter,
            @Named("headerAdministrators") pagingHeader: PagingLoadingAdapter,
            @Named("footerAdministrators") pagingFooter: PagingLoadingAdapter
    ): ConcatAdapter {
        return userListAdapter.withLoadStateHeaderAndFooter(pagingHeader, pagingFooter)
    }

    @PerFragment
    @Provides
    fun provideAddUserBlackListAdapter(imageLoadingDelegate: ImageLoadingDelegate): AddUserBlackListAdapter {
        return AddUserBlackListAdapter(imageLoadingDelegate)
    }

}