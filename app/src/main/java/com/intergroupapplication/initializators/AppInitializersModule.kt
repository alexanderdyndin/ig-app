package com.intergroupapplication.initializators

//import io.realm.Realm
//import io.realm.RealmConfiguration
import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.gsonparserfactory.GsonParserFactory
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.intergroupapplication.di.qualifier.AmazonOkHttpClient
import com.intergroupapplication.domain.exception.*
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
            val config: ImagePipelineConfig = ImagePipelineConfig.newBuilder(app)
                    .setDownsampleEnabled(true)
                    .build()
            Fresco.initialize(app, config)
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
