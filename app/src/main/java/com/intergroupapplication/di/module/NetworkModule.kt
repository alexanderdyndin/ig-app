package com.intergroupapplication.di.module

import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.intergroupapplication.BuildConfig
import com.intergroupapplication.data.mapper.TokenMapper
import com.intergroupapplication.data.model.RefreshTokenModel
import com.intergroupapplication.data.model.TokenModel
import com.intergroupapplication.data.network.*
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.di.qualifier.AmazonOkHttpClient
import com.intergroupapplication.di.qualifier.ApplicationOkHttpClient
import com.intergroupapplication.di.qualifier.RefreshOkHttpClient
import com.intergroupapplication.di.qualifier.TokenInterceptor
import com.intergroupapplication.di.scope.PerApplication
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.io.IOException


@Module
class NetworkModule {

    companion object {
        private const val TOKEN = "Authorization"
        private const val IMEI = "IMEI"
        private const val SERIAL = "SERIAL"
        private const val DEVICE_ID = "DEVICE"
        const val TOKEN_PREFIX = "JWT"
    }

    @PerApplication
    @Provides
    fun providesHttpLoggingInterceptor(): HttpLoggingInterceptor =
            HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    @PerApplication
    @Provides
    @TokenInterceptor
    fun provideTokenInterceptor(sessionStorage: UserSession): Interceptor =
            Interceptor { chain ->
                val imei = sessionStorage.deviceInfoEntity?.imei ?: IMEI
                val serialNumber = sessionStorage.deviceInfoEntity?.serialNumber ?: SERIAL
                val builder = chain.request()
                        .newBuilder()
                        .addHeader(IMEI, imei)
                    builder.addHeader(SERIAL, serialNumber)
                if (sessionStorage.isLoggedIn()) {
                    builder.addHeader(TOKEN, "$TOKEN_PREFIX ${sessionStorage.token?.access}")
                } else {
                    builder.addHeader(DEVICE_ID, sessionStorage.firebaseToken?.token
                            ?: FirebaseMessaging.getInstance().token.result )  //not sure in this command
                }
                val newRequest = builder.build()
                chain.proceed(newRequest)
            }

    @PerApplication
    @Provides
    @ApplicationOkHttpClient
    fun provideApplicationOkHttpClient(logging: HttpLoggingInterceptor,
                                       @TokenInterceptor tokenInterceptor: Interceptor,
                                       tokenAuthenticator: Authenticator): OkHttpClient =
            OkHttpClient.Builder().apply {
                addInterceptor(tokenInterceptor)
                addInterceptor(logging)
                authenticator(tokenAuthenticator)
            }.build()

    @PerApplication
    @Provides
    @AmazonOkHttpClient
    fun provideAmazonOkHttpClient(): OkHttpClient =
            OkHttpClient.Builder().apply {
                retryOnConnectionFailure(true)
            }.build()


    @PerApplication
    @Provides
    @RefreshOkHttpClient
    fun provideRefreshOkHttpClient(logging: HttpLoggingInterceptor,
                                   @TokenInterceptor tokenInterceptor: Interceptor): OkHttpClient =
            OkHttpClient.Builder().apply {
                addInterceptor(tokenInterceptor)
                addInterceptor(logging)
            }.build()

    @PerApplication
    @Provides
    fun provideTokenAuthenticator(sessionStorage: UserSession, tokenMapper: TokenMapper,
                                  api: RefreshTokenApi): Authenticator =
            Authenticator { _, response ->
                if (responseCount(response) >= 2) {
                    return@Authenticator null
                }
                val refreshResult: TokenModel?
                try {
                    refreshResult = sessionStorage.token
                            ?.refresh
                            ?.let {
                                api.refreshAccessToken(RefreshTokenModel("$TOKEN_PREFIX $it"))
                            }?.execute()?.body()
                } catch (e: IOException) {
                    Timber.e(e)
                    return@Authenticator null
                } catch (e: HttpException) {
                    Timber.e(e)
                    return@Authenticator null
                } catch (e: Exception) {
                    Timber.e(e)
                    return@Authenticator null
                }

                if (refreshResult != null) {
                    sessionStorage.token = tokenMapper.mapToDomainEntity(refreshResult)
                    val imei = sessionStorage.deviceInfoEntity?.imei ?: ""
                    val serialNumber = sessionStorage.deviceInfoEntity?.serialNumber
                            ?: ""
                    response.request.newBuilder()
                            .header(TOKEN, "JWT " + sessionStorage.token?.access)
                            .header(IMEI, imei)
                            .header(SERIAL, serialNumber)
                            .header(DEVICE_ID, sessionStorage.firebaseToken?.token!!
                                    /*?: FirebaseMessaging.getInstance().token.result*/ )  //not sure in this command
                            .build()
                } else {
                    null
                }
            }

    private fun responseCount(response: Response): Int {
        var localResponse: Response? = response
        var result = 1
        while (true) {
            localResponse = localResponse?.priorResponse
            if (localResponse != null) {
                result++
            } else {
                break
            }
        }
        return result
    }

    @PerApplication
    @Provides
    fun provideGson(): Gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create()


    @PerApplication
    @Provides
    fun provideRefreshTokenApi(@RefreshOkHttpClient httpClient: OkHttpClient, gson: Gson)
            : RefreshTokenApi = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .client(httpClient)
            .baseUrl(BuildConfig.BASE_URL)
            .build().create(RefreshTokenApi::class.java)

    @PerApplication
    @Provides
    fun provideAmazonApi(@AmazonOkHttpClient httpClient: OkHttpClient,
                         gson: Gson): AmazonApi = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .client(httpClient)
            .baseUrl(BuildConfig.AMAZON_BASE_URL)
            .build().create(AmazonApi::class.java)

    @PerApplication
    @Provides
    fun provideAppApi(@ApplicationOkHttpClient httpClient: OkHttpClient, gson: Gson,
                      errorAdapter: ErrorAdapter): AppApi = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxErrorCallAdapterFactory.create(errorAdapter))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .client(httpClient)
            .baseUrl(BuildConfig.BASE_URL)
            .build().create(AppApi::class.java)


}
