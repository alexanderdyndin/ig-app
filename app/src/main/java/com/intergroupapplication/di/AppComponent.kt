package com.intergroupapplication.di

import com.intergroupapplication.App
import com.intergroupapplication.di.module.*
import com.intergroupapplication.di.scope.PerApplication
import com.intergroupapplication.initializators.AppInitializersModule
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule

@PerApplication
@Component(modules = [(AppModule::class),
    (RxModule::class),
    (NetworkModule::class),
    (AppBuilderModule::class),
    (AndroidSupportInjectionModule::class),
    (GatewayModule::class),
    (CoreModule::class),
    (AppInitializersModule::class),
    (ListenersModule::class),
    (NetworkErrorHandlingModule::class),
    (NotificationModule::class),
    (ViewModelModule::class)])
interface AppComponent : AndroidInjector<App> {

    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<App>()

}
