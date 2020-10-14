package com.intergroupapplication.presentation.feature.grouplist.di

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.feature.grouplist.adapter.GroupListAdapter
import com.intergroupapplication.presentation.feature.grouplist.view.GroupListFragment
import com.intergroupapplication.presentation.feature.navigation.view.NavigationActivity
import dagger.Module
import dagger.Provides
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import javax.inject.Named

@Module
class GroupListViewModule {

    @PerFragment
    @Provides
    fun provideGroupPostEntityDiffUtilCallback() = object : DiffUtil.ItemCallback<GroupEntity>() {
        override fun areItemsTheSame(oldItem: GroupEntity, newItem: GroupEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: GroupEntity, newItem: GroupEntity) = oldItem == newItem
    }

//    @PerFragment
//    @Provides
//    fun provideGroupList(diffUtil: DiffUtil.ItemCallback<GroupEntity>,
//                                imageLoadingDelegate: ImageLoadingDelegate): GroupListAdapter =
//            GroupListAdapter(diffUtil, imageLoadingDelegate)

    @PerFragment
    @Provides
    fun provideSupportAppNavigator(activity: NavigationActivity): SupportAppNavigator =
            SupportAppNavigator(activity, 0)

    @PerFragment
    @Provides
    fun provideLinearLayoutManager(fragment: GroupListFragment): LinearLayoutManager =
            LinearLayoutManager(fragment.context, LinearLayoutManager.VERTICAL, false)

}
