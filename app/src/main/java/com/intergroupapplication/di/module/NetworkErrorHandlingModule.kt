package com.intergroupapplication.di.module

import com.intergroupapplication.data.network.BaseErrorAdapter
import com.intergroupapplication.data.network.BaseErrorParser
import com.intergroupapplication.data.network.ErrorAdapter
import com.intergroupapplication.data.network.ErrorParser
import com.intergroupapplication.di.scope.PerApplication
import dagger.Binds
import dagger.Module

/**
 * Created by abakarmagomedov on 24/08/2018 at project InterGroupApplication.
 */
@Module
interface NetworkErrorHandlingModule {

    @PerApplication
    @Binds
    fun provideErrorParser(baseErrorParser: BaseErrorParser): ErrorParser

    @PerApplication
    @Binds
    fun provideErrorAdapter(baseErrorAdapter: BaseErrorAdapter): ErrorAdapter

}