package com.intergroupapplication.initializators

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.gsonparserfactory.GsonParserFactory
import com.appodeal.ads.Appodeal
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.android.gms.ads.MobileAds
import com.intergroupapplication.BuildConfig
import com.intergroupapplication.data.repository.PhotoRepository
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.di.qualifier.AmazonOkHttpClient
import com.intergroupapplication.domain.exception.*
import com.intergroupapplication.presentation.feature.mainActivity.view.MainActivity
import com.jakewharton.threetenabp.AndroidThreeTen
import com.miguelbcr.ui.rx_paparazzo2.RxPaparazzo
import com.workable.errorhandler.ErrorHandler
import com.workable.errorhandler.Matcher
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import es.dmoral.toasty.Toasty
//import io.realm.Realm
//import io.realm.RealmConfiguration
import okhttp3.OkHttpClient
import retrofit2.HttpException
import java.lang.IllegalStateException
import java.net.ConnectException
import java.net.UnknownHostException
import kotlin.coroutines.coroutineContext

/**
 * Created by abakarmagomedov on 10/08/2018 at project InterGroupApplication.
 */

@Module
class AppInitializersModule {


//    @Provides
//    @IntoSet
//    fun provideRealmInitializer(context: Context): Initializer = object : Initializer {
//        override fun initialize(app: Application) {
//            Realm.init(context)
//            val realmConfiguration = RealmConfiguration.Builder().name("intergroup,database").build()
//            Realm.setDefaultConfiguration(realmConfiguration)
//        }
//    }

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
    fun provideFrecoInitializer(): Initializer = object : Initializer {
        override fun initialize(app: Application) {
            Fresco.initialize(app)
        }
    }

//    @Provides
//    @IntoSet
//    fun provideAdMobInitializer(context: Context): Initializer = object : Initializer {
//        override fun initialize(app: Application) {
//            MobileAds.initialize(context, "ca-app-pub-1717383973096668~8939439101")
//        }
//    }


    @Provides
    @IntoSet
    fun provideRxPapparazzoInitializer(): Initializer = object : Initializer {
        override fun initialize(app: Application) {
            RxPaparazzo.register(app)
        }
    }

    @Provides
    @IntoSet
    fun provideAndroidNetworkingInitializer(@AmazonOkHttpClient okHttpClient: OkHttpClient,
                                            context: Context,
                                            gsonParserFactory: GsonParserFactory) = object : Initializer {
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
