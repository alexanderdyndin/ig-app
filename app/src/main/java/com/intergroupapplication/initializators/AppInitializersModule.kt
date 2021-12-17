package com.intergroupapplication.initializators

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.gsonparserfactory.GsonParserFactory
import com.facebook.drawee.backends.pipeline.Fresco
import com.intergroupapplication.di.qualifier.AmazonOkHttpClient
import com.jakewharton.threetenabp.AndroidThreeTen
import com.miguelbcr.ui.rx_paparazzo2.RxPaparazzo
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import es.dmoral.toasty.Toasty
import okhttp3.OkHttpClient


/**
 * Created by abakarmagomedov on 10/08/2018 at project InterGroupApplication.
 */

@Module
class AppInitializersModule {

    @Provides
    @IntoSet
    fun provideAppCompatInitializer(): Initializer = object : Initializer {
        override fun initialize(app: Application) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }

    }

    @Provides
    @IntoSet
    fun provideTimeInitializer(): Initializer = object : Initializer {
        override fun initialize(app: Application) {
            AndroidThreeTen.init(app)
        }
    }

    @Provides
    @IntoSet
    fun provideFrescoInitializer(): Initializer = object : Initializer {
        override fun initialize(app: Application) {
            Fresco.initialize(app)
        }
    }


    @Provides
    @IntoSet
    fun provideRxPaparazzoInitializer(): Initializer = object : Initializer {
        override fun initialize(app: Application) {
            RxPaparazzo.register(app)
        }
    }

    @Provides
    @IntoSet
    fun provideAndroidNetworkingInitializer(
        @AmazonOkHttpClient okHttpClient: OkHttpClient,
        context: Context,
        gsonParserFactory: GsonParserFactory
    ) = object : Initializer {
        override fun initialize(app: Application) {
            AndroidNetworking.initialize(context, okHttpClient)
            AndroidNetworking.setParserFactory(gsonParserFactory)
        }

    }

    @Provides
    @IntoSet
    fun provideToastyInitializer(): Initializer = object : Initializer {
        override fun initialize(app: Application) {
            Toasty.Config.getInstance().apply()
        }

    }
}
