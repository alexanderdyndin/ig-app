package com.intergroupapplication.presentation.feature.userlist.di

import android.content.Context
import androidx.recyclerview.widget.ConcatAdapter
import com.intergroupapplication.di.qualifier.*
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.presentation.base.FrescoImageLoader
import com.intergroupapplication.presentation.base.ImageLoader
import com.intergroupapplication.presentation.base.adapter.PagingLoadingAdapter
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.feature.addBlackListById.adapter.AddUserBlackListAdapter
import com.intergroupapplication.presentation.feature.userlist.adapter.TypeUserList
import com.intergroupapplication.presentation.feature.userlist.adapter.UserListAdapter
import com.intergroupapplication.presentation.feature.userlist.view.UserListFragment
import com.intergroupapplication.presentation.manager.DialogManager
import com.intergroupapplication.presentation.provider.DialogProvider
import dagger.Module
import dagger.Provides

@Module
class UserListViewModule {
    @PerFragment
    @Provides
    fun provideDialogManager(fragment: UserListFragment): DialogManager =
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
    fun provideFrescoImageLoader(): ImageLoader =
        FrescoImageLoader()

    @PerFragment
    @Provides
    fun provideImageLoadingDelegate(imageLoader: ImageLoader): ImageLoadingDelegate =
        ImageLoadingDelegate(imageLoader)

    @PerFragment
    @Provides
    @All
    fun provideUserListAdapterAll(imageLoadingDelegate: ImageLoadingDelegate): UserListAdapter {
        return UserListAdapter(imageLoadingDelegate, TypeUserList.ALL)
    }

    @PerFragment
    @Provides
    @Blocked
    fun provideUserListAdapterBlocked(imageLoadingDelegate: ImageLoadingDelegate): UserListAdapter {
        return UserListAdapter(imageLoadingDelegate, TypeUserList.BLOCKED)
    }

    @PerFragment
    @Provides
    @Administrators
    fun provideUserListAdapterAdministrators(imageLoadingDelegate: ImageLoadingDelegate)
            : UserListAdapter {
        return UserListAdapter(imageLoadingDelegate, TypeUserList.ADMINISTRATORS)
    }

    @PerFragment
    @Provides
    @HeaderAll
    fun provideHeaderAdapterAll(@All userListAdapter: UserListAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { userListAdapter.retry() }
    }

    @PerFragment
    @Provides
    @FooterAll
    fun provideFooterAdapterAll(@All userListAdapter: UserListAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { userListAdapter.retry() }
    }

    @PerFragment
    @Provides
    @HeaderBlocked
    fun provideHeaderAdapterBlocked(@Blocked userListAdapter: UserListAdapter): PagingLoadingAdapter {
        return PagingLoadingAdapter { userListAdapter.retry() }
    }

    @PerFragment
    @Provides
    @FooterBlocked
    fun provideFooterAdapterBlocked(@Blocked userListAdapter: UserListAdapter)
            : PagingLoadingAdapter {
        return PagingLoadingAdapter { userListAdapter.retry() }
    }

    @PerFragment
    @Provides
    @HeaderAdministrators
    fun provideHeaderAdapterAdministrators(@Administrators userListAdapter: UserListAdapter)
            : PagingLoadingAdapter {
        return PagingLoadingAdapter { userListAdapter.retry() }
    }

    @PerFragment
    @Provides
    @FooterAdministrators
    fun provideFooterAdapterAdministrators(@Administrators userListAdapter: UserListAdapter)
            : PagingLoadingAdapter {
        return PagingLoadingAdapter { userListAdapter.retry() }
    }

    @PerFragment
    @Provides
    @All
    fun provideConcatListAll(
        @All userListAdapter: UserListAdapter,
        @HeaderAll pagingHeader: PagingLoadingAdapter,
        @FooterAll pagingFooter: PagingLoadingAdapter
    ): ConcatAdapter {
        return userListAdapter.withLoadStateHeaderAndFooter(pagingHeader, pagingFooter)
    }

    @PerFragment
    @Provides
    @Blocked
    fun provideConcatListBlocked(
        @Blocked userListAdapter: UserListAdapter,
        @HeaderBlocked pagingHeader: PagingLoadingAdapter,
        @FooterBlocked pagingFooter: PagingLoadingAdapter
    ): ConcatAdapter {
        return userListAdapter.withLoadStateHeaderAndFooter(pagingHeader, pagingFooter)
    }

    @PerFragment
    @Provides
    @Administrators
    fun provideConcatListAdministrators(
        @Administrators userListAdapter: UserListAdapter,
        @HeaderAdministrators pagingHeader: PagingLoadingAdapter,
        @FooterAdministrators pagingFooter: PagingLoadingAdapter
    ): ConcatAdapter {
        return userListAdapter.withLoadStateHeaderAndFooter(pagingHeader, pagingFooter)
    }

    @PerFragment
    @Provides
    fun provideAddUserBlackListAdapter(imageLoadingDelegate: ImageLoadingDelegate)
            : AddUserBlackListAdapter {
        return AddUserBlackListAdapter(imageLoadingDelegate)
    }

}