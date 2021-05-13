package com.intergroupapplication.di.module

import android.R.attr
import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import com.intergroupapplication.App
import com.intergroupapplication.R
import com.intergroupapplication.di.scope.PerApplication
import com.intergroupapplication.domain.crypto.EncryptionDelegate
import com.intergroupapplication.domain.crypto.Encryptor
import com.intergroupapplication.presentation.base.BaseEncryptor
import com.intergroupapplication.presentation.base.FrescoMemoryTrimmableRegistry
import dagger.Module
import dagger.Provides

/**
 * Created by abakarmagomedov on 06/08/2018 at project InterGroupApplication.
 */

@Module
class CoreModule {

    @PerApplication
    @Provides
    fun provideEncryptionDelegate(): EncryptionDelegate = EncryptionDelegate()


    @PerApplication
    @Provides
    fun provideBaseEncryptor(encryptionDelegate: EncryptionDelegate): Encryptor =
            BaseEncryptor(encryptionDelegate)

    @PerApplication
    @Provides
    fun provideSharedPreferences(context: Context): SharedPreferences =
            context.getSharedPreferences(App::class.java.simpleName, Context.MODE_PRIVATE)

    @PerApplication
    @Provides
    fun provideRadioButtonColorStateList(context: Context): ColorStateList =
            ColorStateList(arrayOf(intArrayOf(-attr.state_checked), intArrayOf(attr.state_checked)),
                    intArrayOf(ContextCompat.getColor(context, R.color.radioButtonCircleColor),
                            ContextCompat.getColor(context, R.color.colorAccent)))

    @PerApplication
    @Provides
    fun provideFrescoMemoryTrim(): FrescoMemoryTrimmableRegistry = FrescoMemoryTrimmableRegistry()
}
