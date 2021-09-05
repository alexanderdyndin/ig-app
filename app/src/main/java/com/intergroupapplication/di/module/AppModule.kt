package com.intergroupapplication.di.module

import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.androidnetworking.gsonparserfactory.GsonParserFactory
import com.danikula.videocache.HttpProxyCacheServer
import com.facebook.common.util.UriUtil
import com.intergroupapplication.App
import com.intergroupapplication.R
import com.intergroupapplication.di.qualifier.DashDateFormatter
import com.intergroupapplication.di.qualifier.PointDateFormatter
import com.intergroupapplication.di.scope.PerApplication
import com.workable.errorhandler.ErrorHandler
import com.yalantis.ucrop.UCrop
import dagger.Module
import dagger.Provides
import id.zelory.compressor.Compressor
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


@Module
class AppModule {

    @PerApplication
    @Provides
    fun provideContext(application: App): Context = application

    @PerApplication
    @Provides
    fun provideGsonParserFactory(): GsonParserFactory = GsonParserFactory()

    @PerApplication
    @Provides
    fun provideTelephonyManager(context: Context): TelephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    @PerApplication
    @Provides
    fun provideErrorHandler(): ErrorHandler = ErrorHandler.defaultErrorHandler()

    @PerApplication
    @Provides
    @PointDateFormatter
    fun provideDatePointFormatter(): DateFormat =
            SimpleDateFormat("dd.mm.yyyy", Locale.getDefault())

    @PerApplication
    @Provides
    fun provideImageCompressor(context: Context): Compressor =
            Compressor(context)

    @PerApplication
    @Provides
    fun provideContentResolver(context: Context): ContentResolver = context.contentResolver

    @PerApplication
    @Provides
    fun provideHttpProxyCacheServer(application: App): HttpProxyCacheServer =
            HttpProxyCacheServer.Builder(application)
                    .maxCacheSize(1024*1024*1024)
                    .build()

    @PerApplication
    @Provides
    @DashDateFormatter
    fun provideDateDashFormatter(): DateFormat =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    @PerApplication
    @Provides
    fun provideNotificationManager(context: Context): NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @PerApplication
    @Provides
    fun provideCropOptions(context: Context): UCrop.Options =
            UCrop.Options().apply {
                setToolbarColor(ContextCompat.getColor(context, R.color.appBackgroundColor))
                setStatusBarColor(ContextCompat.getColor(context, R.color.appBackgroundColor))
                setActiveWidgetColor(ContextCompat.getColor(context, R.color.colorAccent))
            }


    @PerApplication
    @Provides
    fun provideApplicationLogoUri(): Uri = Uri.Builder()
            .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
            .path(R.drawable.application_logo.toString())
            .build()


}
