package com.intergroupapplication.presentation.manager

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings;
import android.provider.Settings.System;
import android.telephony.TelephonyManager
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 03/08/2018 at project InterGroupApplication.
 */
class PhoneCharacteristicManager @Inject constructor(private val telephonyManager: TelephonyManager,
                                                     private val contentResolver: ContentResolver,
                                                     private val context: Context) {


    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission", "HardwareIds")
    fun getImei(): String = "sanya_hui_sosi"
//    {
//        try {
//            val imei = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                telephonyManager.imei
//            } else {
//                telephonyManager.deviceId
//            })
//            return if (imei == null || imei.isEmpty()) "sanya_hui_sosi" else imei
//        } catch (exception: Throwable) {
//            return "sanya_hui_sosi"
//        }
//    }

    @SuppressLint("HardwareIds")
    fun getMac(): String {
        val manager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = manager.connectionInfo
        return info.macAddress.toUpperCase(java.util.Locale.getDefault())
    }


    fun getSerialNumber(): String = "jopa"
//            UUID.randomUUID().toString()

}
