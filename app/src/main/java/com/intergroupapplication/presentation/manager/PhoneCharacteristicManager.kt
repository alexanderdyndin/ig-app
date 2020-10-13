package com.intergroupapplication.presentation.manager

import android.annotation.SuppressLint
import github.nisrulz.easydeviceinfo.base.EasyDeviceMod
import github.nisrulz.easydeviceinfo.common.EasyDeviceInfo
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 03/08/2018 at project InterGroupApplication.
 */
class PhoneCharacteristicManager @Inject constructor(private val easyDeviceMod: EasyDeviceMod) {


    @SuppressLint("MissingPermission")
    fun getImei(): String = easyDeviceMod.imei

    @SuppressLint("MissingPermission")
    fun getSerialNumber(): String {
        val serial = easyDeviceMod.serial
        return if (serial == EasyDeviceInfo.notFoundVal) {
            ""
        } else {
            serial
        }
    }

}
