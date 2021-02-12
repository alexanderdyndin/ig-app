package com.intergroupapplication.presentation.manager

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.os.Build
import android.provider.Settings;
import android.provider.Settings.System;
import android.telephony.TelephonyManager
import java.util.*
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 03/08/2018 at project InterGroupApplication.
 */
class PhoneCharacteristicManager @Inject constructor(private val telephonyManager: TelephonyManager,
                                                     private val contentResolver: ContentResolver) {


    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission", "HardwareIds")
    fun getImei(): String {
        return (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            telephonyManager.imei
        } else {
            telephonyManager.deviceId
        }) ?: System.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }


    fun getSerialNumber(): String = UUID.randomUUID().toString()

}
