package com.intergroupapplication.presentation.feature.navigation.di

import com.intergroupapplication.di.scope.PerActivity
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.presentation.feature.grouplist.di.GroupListViewModule
import com.intergroupapplication.presentation.feature.grouplist.view.GroupListFragment
import com.intergroupapplication.presentation.feature.news.di.NewsViewModule
import com.intergroupapplication.presentation.feature.news.view.NewsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by abakarmagomedov on 16/10/2018 at project InterGroupApplication.
 */
//@Module
//interface NavigationBuilderModule {
//
//    @PerFragment
//    @ContributesAndroidInjector(modules = [GroupListViewModule::class])
//    fun provideGroupListFragmentFactory(): GroupListFragment
//
//    @PerFragment
//    @ContributesAndroidInjector(modules = [NewsViewModule::class])
//    fun provideNewsFragmentFactory(): NewsFragment
//}
