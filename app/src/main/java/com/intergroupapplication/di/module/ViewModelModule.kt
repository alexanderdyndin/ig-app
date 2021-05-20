package com.intergroupapplication.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.intergroupapplication.di.qualifier.ViewModelKey
import com.intergroupapplication.presentation.factory.ViewModelFactory
import com.intergroupapplication.presentation.feature.audiolist.viewModel.AudioListViewModel
import com.intergroupapplication.presentation.feature.commentsbottomsheet.viewmodel.BottomViewModel
import com.intergroupapplication.presentation.feature.commentsdetails.viewmodel.CommentsViewModel
import com.intergroupapplication.presentation.feature.group.viewmodel.GroupViewModel
import com.intergroupapplication.presentation.feature.grouplist.viewModel.GroupListViewModel
import com.intergroupapplication.presentation.feature.mainActivity.viewModel.MainActivityViewModel
import com.intergroupapplication.presentation.feature.news.viewmodel.NewsViewModel
import com.intergroupapplication.presentation.feature.userlist.addBlackListById.AddBlackListByIdViewModel
import com.intergroupapplication.presentation.feature.userlist.viewModel.UserListViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(NewsViewModel::class)
    internal abstract fun bindNewsViewModel(newsViewModel: NewsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GroupListViewModel::class)
    internal abstract fun bindGroupsViewModel(groupListViewModel: GroupListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MainActivityViewModel::class)
    internal abstract fun bindMainActivityViewModel(mainActivityViewModel: MainActivityViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserListViewModel::class)
    internal abstract fun bindUserListViewModel(userListViewModel: UserListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AddBlackListByIdViewModel::class)
    internal abstract fun bindAddBlackListByIdViewModel(addBlackListByIdViewModel: AddBlackListByIdViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GroupViewModel::class)
    internal abstract fun bindGroupViewModel(groupViewModel: GroupViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CommentsViewModel::class)
    internal abstract fun bindCommentsViewModel(commentsViewModel: CommentsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BottomViewModel::class)
    internal abstract fun bindBottomViewModel(commentsViewModel: BottomViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AudioListViewModel::class)
    internal abstract fun bindAudiosViewModel(audiosViewModel: AudioListViewModel): ViewModel

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}